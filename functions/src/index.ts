import {
  onDocumentCreated,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// Inicializa el SDK de administrador si no se ha hecho
if (admin.apps.length === 0) {
  admin.initializeApp();
}

/**
 * Esta función se dispara cada vez que se crea un nuevo documento de mensaje.
 */
export const sendReferralNotification = onDocumentCreated(
  {
    document: "messages/{id}",
    memory: "512MiB",
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.log("No se encontraron datos en el evento.");
      return;
    }
    const messageData = snapshot.data();

    const referralId = messageData.referralId;
    const sendId = messageData.senderId;
    const receiverId = messageData.receiverId;
    const subject = messageData.subject;
    const content = messageData.content;

    logger.log(`New message in referral ${referralId}. Sending...`);

    const senderDoc = await admin.firestore()
      .collection("users").doc(sendId).get();
    const senderName = senderDoc.data()?.name || "Anonymous";

    const userDoc = await admin.firestore()
      .collection("users").doc(receiverId).get();
    const userData = userDoc.data();

    if (userData?.fcmToken) {
      const fcmToken = userData.fcmToken;
      // Construye el payload de la notificación
      const payload = {
        token: fcmToken,
        data: {
          senderName: senderName,
          subject: subject || "No attachment",
          content: content || "No content",
          referralId: referralId,
        },
      };

      logger.log(`Enviando notificación a ${receiverId}`);
      try {
        await admin.messaging().send(payload);
      } catch (error) {
        logger.error(`Error al enviar notificación a ${receiverId}`, error);
      }
    } else {
      logger.log(`Token FCM no encontrado para el usuario ${receiverId}`);
    }
  }
);

/**
 * Propaga los cambios del perfil del proveedor a sus productos denormalizados.
 */
export const onProviderProfileUpdated = onDocumentUpdated(
  {
    document: "users/{uid}",
    memory: "512MiB",
    timeoutSeconds: 120,
  },
  async (event) => {
    const newData = event.data?.after.data();
    const previousData = event.data?.before.data();

    if (!newData || !previousData) return;
    if (newData.type !== "PROVIDER") return;

    const nameChanged = newData.name !== previousData.name;
    const photoChanged = newData.photoUrl !== previousData.photoUrl;
    const ratingChanged = newData.paymentRating !== previousData.paymentRating;
    const industryChanged = newData.industry !== previousData.industry;

    // Línea dividida para no superar los 80 caracteres
    const hasChanged = nameChanged || photoChanged ||
                       ratingChanged || industryChanged;

    if (!hasChanged) {
      return;
    }

    const uid = event.params.uid;
    logger.log(`Iniciando propagación para el proveedor ${uid}...`);

    const db = admin.firestore();
    const productsRef = db.collection("products_provider");

    const updates: Record<string, unknown> = {};
    if (nameChanged) updates.providerName = newData.name;
    if (photoChanged) updates.providerPhotoUrl = newData.photoUrl;
    if (ratingChanged) updates.providerRating = newData.paymentRating;
    if (industryChanged) updates.industry = newData.industry;

    const BATCH_LIMIT = 500;
    let lastDoc: admin.firestore.QueryDocumentSnapshot | null = null;
    let totalUpdated = 0;
    let hasMore = true;

    while (hasMore) {
      let query = productsRef
        .where("providerId", "==", uid)
        .orderBy("__name__")
        .limit(BATCH_LIMIT);

      if (lastDoc) {
        query = query.startAfter(lastDoc);
      }

      const snapshot = await query.get();
      if (snapshot.empty) {
        hasMore = false;
        break;
      }

      const batch = db.batch();
      snapshot.docs.forEach((doc) => {
        batch.update(doc.ref, updates);
      });

      await batch.commit();
      totalUpdated += snapshot.size;
      logger.log(`Batch procesado. Total actualizado: ${totalUpdated}`);

      lastDoc = snapshot.docs[snapshot.docs.length - 1];
      if (snapshot.size < BATCH_LIMIT) {
        hasMore = false;
      }
    }

    logger.log(`Actualización finalizada. Total productos: ${totalUpdated}`);
  }
);
