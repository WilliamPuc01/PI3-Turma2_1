import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as QRCode from "qrcode";
import { v4 as uuidv4 } from "uuid";

admin.initializeApp();
const db = admin.firestore();

export const performAuth = functions.https.onRequest(async (req, res) => {
  const { apiKey, siteUrl } = req.body;

  if (!apiKey || !siteUrl) {
    res.status(400).json({ error: "Dados incompletos" });
    return;
  }

  const partnerDoc = await db.collection("partners").doc(siteUrl).get();
  if (!partnerDoc.exists || partnerDoc.data()?.apiKey !== apiKey) {
    res.status(401).json({ error: "Parceiro inválido" });
    return;
  }

  const token = uuidv4().replace(/-/g, "") + uuidv4().replace(/-/g, "");
  await db.collection("login").doc(token).set({
    apiKey,
    loginToken: token,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  const qrBase64 = await QRCode.toDataURL(token);
  res.status(200).json({ qrCode: qrBase64, loginToken: token });
});
