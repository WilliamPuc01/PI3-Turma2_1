import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as QRCode from "qrcode";
import {v4 as uuidv4} from "uuid";
import cors from "cors";

admin.initializeApp();
const db = admin.firestore();
const corsHandler = cors({origin: true});

// ===== Função 1: Geração do QR Code =====

export const performAuth = functions.https.onRequest((req, res) => {
  corsHandler(req, res, async () => {
    try {
      const contentType = req.get("content-type");

      let body = req.body;
      if (
        contentType?.includes("application/json") &&
        typeof req.body === "string"
      ) {
        body = JSON.parse(req.body);
      }

      const {apiKey, siteUrl} = body;

      if (!apiKey || !siteUrl) {
        return res.status(400).json({
          error: "Dados incompletos",
        });
      }

      const partnerDoc = await db.collection("partners")
        .doc(siteUrl)
        .get();

      if (!partnerDoc.exists || partnerDoc.data()?.apiKey !== apiKey) {
        return res.status(401).json({
          error: "Parceiro inválido",
        });
      }

      const token = uuidv4().replace(/-/g, "") + uuidv4().replace(/-/g, "");

      await db.collection("login").doc(token).set({
        apiKey,
        loginToken: token,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        attempts: 0,
      });

      const qrBase64 = await QRCode.toDataURL(token);

      return res.status(200).json({
        qrCode: qrBase64,
        loginToken: token,
      });
    } catch (error) {
      console.error("Erro na função performAuth:", error);
      return res.status(500).json({
        error: "Erro interno do servidor",
      });
    }
  });
});

// ===== Função 2: Verificação de login =====

export const getLoginStatus = functions.https.onRequest((req, res) => {
  corsHandler(req, res, async () => {
    try {
      const {loginToken} = req.body;

      if (!loginToken) {
        return res.status(400).json({
          error: "Token ausente",
        });
      }

      const docRef = db.collection("login").doc(loginToken);
      const doc = await docRef.get();

      if (!doc.exists) {
        return res.status(404).json({
          error: "Token inválido",
        });
      }

      const data = doc.data();
      const attempts = data?.attempts ?? 0;
      const createdAt = data?.createdAt?.toDate?.();
      const now = new Date();

      const expired =
        !createdAt || now.getTime() - createdAt.getTime() > 60 * 1000;

      if (data?.status === "confirmado") {
        return res.status(200).json({
          status: "confirmado",
          uid: data.uid,
          loginTime: data.timestamp,
        });
      }

      if (expired || attempts >= 2) {
        await docRef.delete();
        return res.status(410).json({
          error: "Token expirado ou excedeu tentativas",
        });
      }

      await docRef.update({
        attempts: admin.firestore.FieldValue.increment(1),
      });

      return res.status(202).json({
        status: "aguardando confirmação",
      });
    } catch (error) {
      console.error("Erro na função getLoginStatus:", error);
      return res.status(500).json({
        error: "Erro interno do servidor",
      });
    }
  });
});
