import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// Inicializa el SDK de administrador
admin.initializeApp();

/**
 * Esta función se dispara cada vez que se crea un nuevo documento de mensaje.
 */
export const sendReferralNotification = onDocumentCreated(
  "messages/{id}",
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
