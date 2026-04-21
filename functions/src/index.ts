import {
  onDocumentCreated,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";
import {onRequest} from "firebase-functions/v2/https";
import axios from "axios";

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

/**
 * Webhook para recibir notificaciones de pagos desde PayPhone.
 */
export const payphoneWebhook = onRequest(
  {secrets: ["PAYPHONE_WEBHOOK_SECRET"]},
  async (req, res) => {
    // Seguridad validar Token secreto en la URL (?secret=)
    const WEBHOOK_SECRET = process.env.PAYPHONE_WEBHOOK_SECRET;
    if (req.query.secret !== WEBHOOK_SECRET) {
      logger.warn("Intento de acceso no autorizado al Webhook.");
      res.status(403).json({"Response": false, "ErrorCode": "222"});
      return;
    }

    // Validar que sea un POST
    if (req.method !== "POST") {
      res.status(405).json({"Response": false, "ErrorCode": "222"});
      return;
    }

    const data = req.body;

    // Validar si los datos necesarios vienen en el body
    if (!data || !data.TransactionId || !data.ClientTransactionId) {
      res.status(400).json({"Response": false, "ErrorCode": "444"});
      return;
    }

    // Solo procesamos si el estado es aprobado
    if (data.TransactionStatus === "Approved" && data.StatusCode === 3) {
      const shortId = data.ClientTransactionId; // ID corto de 15 chars
      const transactionId = data.TransactionId;
      const amount = data.Amount;
      const authorizationCode = data.AuthorizationCode;

      try {
        const db = admin.firestore();

        // 1. Verificar si el ID corto existe en los intentos de pago
        const attemptDoc = await db.collection("payment_attempts")
          .doc(shortId).get();

        if (!attemptDoc.exists) {
          logger.error(`Intento de pago no encontrado para el ID: ${shortId}`);
          res.status(404).json({"Response": false, "ErrorCode": "444"});
          return;
        }

        const realUid = attemptDoc.data()?.uid; // El UID de 28 caracteres

        // 2. Verificar si la transacción ya fue procesada anteriormente
        const paymentDoc = await db.collection("payments")
          .doc(transactionId.toString()).get();

        if (paymentDoc.exists) {
          res.status(200).json({"Response": false, "ErrorCode": "333"});
          return;
        }

        // 3. Actualizar el usuario real de 28 caracteres
        await db.runTransaction(async (t) => {
          const userRef = db.collection("users").doc(realUid);
          const userDoc = await t.get(userRef);

          if (!userDoc.exists) throw new Error("User real not found");

          const userData = userDoc.data();
          const currentLimit = userData?.referralLimit || 100;

          // Aumentar el límite
          t.update(userRef, {referralLimit: currentLimit + 100});

          // Guardar registro del pago vinculado al UID real
          const paymentId = transactionId.toString();
          const paymentRef = db.collection("payments").doc(paymentId);
          t.set(paymentRef, {
            id: paymentId,
            shortId: shortId,
            uid: realUid,
            amount: amount / 100,
            authorizationCode: authorizationCode,
            date: admin.firestore.FieldValue.serverTimestamp(),
            status: "SUCCESS",
          });

          // Opcional: Marcar el intento como procesado
          t.update(attemptDoc.ref, {status: "PROCESSED"});
        });

        logger.log(`Pago aprobado y procesado para el usuario: ${realUid}`);

        res.status(200).json({"Response": true, "ErrorCode": "000"});
      } catch (error) {
        logger.error("Error procesando notificación de PayPhone:", error);
        res.status(500).json({"Response": false, "ErrorCode": "222"});
      }
    } else {
      res.status(200).json({"Response": true, "ErrorCode": "000"});
    }
  }
);

/**
 * Genera un link de pago dinámico vinculado a un usuario de 28 caracteres.
 */
export const createPaymentLink = onRequest(
  {
    secrets: ["PAYPHONE_TOKEN"],
    region: "us-central1",
    memory: "512MiB",
  },
  async (req, res) => {
    if (req.method !== "POST") {
      res.status(405).send("Method Not Allowed");
      return;
    }

    try {
      const {uid} = req.body;
      if (!uid) {
        res.status(400).json({error: "Falta uid en el body"});
        return;
      }

      // 1. Obtener y verificar el token
      const token = (process.env.PAYPHONE_TOKEN || "").trim();
      const storeId = "dbbba11d-a472-466a-8831-bc01b4355e5b";

      if (!token) {
        throw new Error("Secret PAYPHONE_TOKEN no configurado");
      }

      // 2. Generar clientTransactionId ÚNICO (solo números)
      const uniqueId = Date.now().toString().slice(-14);

      // 3. Payload exacto que funcionó en Insomnia
      const payload = {
        amount: 115,
        amountWithTax: 100,
        amountWithoutTax: 0,
        tax: 15,
        service: 0,
        tip: 0,
        currency: "USD",
        clientTransactionId: uniqueId,
        storeId: storeId,
        reference: "Pago WinApp",
        oneTime: true,
      };

      logger.log(`Iniciando petición a PayPhone con Axios. ID: ${uniqueId}`);

      // 4. Petición usando Axios
      const response = await axios.post(
        "https://pay.payphonetodoesposible.com/api/Links",
        payload,
        {
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      // PayPhone devuelve el link (a veces como string plano con comillas)
      const cleanLink = response.data.trim().replace(/^"|"$/g, "");

      // 5. Guardar el intento para el webhook
      await admin.firestore().collection("payment_attempts").doc(uniqueId).set({
        uid: uid,
        shortId: uniqueId,
        amount: 1.15,
        status: "PENDING",
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      res.status(200).json({
        url: cleanLink,
        shortId: uniqueId,
      });
    } catch (e: unknown) {
      logger.error("Error en createPaymentLink (Axios):", e);
      res.status(500).json({
        error: "PayPhone API Error",
        details: e,
      });
    }
  }
);
