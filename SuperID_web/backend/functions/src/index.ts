import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as QRCode from "qrcode";
import {v4 as uuidv4} from "uuid";

admin.initializeApp();
const db = admin.firestore();

// Geração do QR Code e token
export const performAuth = functions.https.onRequest(async (req, res) => {
  const {apiKey, siteUrl} = req.body;

  if (!apiKey || !siteUrl) {
    res.status(400).json({error: "Dados incompletos"});
    return;
  }

  const partnerDoc = await db.collection("partners").doc(siteUrl).get();
  if (!partnerDoc.exists || partnerDoc.data()?.apiKey !== apiKey) {
    res.status(401).json({error: "Parceiro inválido"});
    return;
  }

  const token = uuidv4().replace(/-/g, "") + uuidv4().replace(/-/g, "");
  await db.collection("login").doc(token).set({
    apiKey,
    loginToken: token,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    attempts: 0, // Número inicial de tentativas
  });

  const qrBase64 = await QRCode.toDataURL(token);
  res.status(200).json({qrCode: qrBase64, loginToken: token});
});

// Consulta do status de login
export const getLoginStatus = functions.https.onRequest(async (req, res) => {
  const {loginToken} = req.body;

  if (!loginToken) {
    res.status(400).json({error: "Token ausente"});
    return;
  }

  const docRef = db.collection("login").doc(loginToken);
  const doc = await docRef.get();

  if (!doc.exists) {
    res.status(404).json({error: "Token inválido"});
    return;
  }

  const data = doc.data();
  const attempts = data?.attempts ?? 0;

  const createdAt = data?.createdAt?.toDate?.();
  const now = new Date();
  const expired =
  !createdAt || (now.getTime() - createdAt.getTime() > 60 * 1000);

  if (attempts >= 3 || expired) {
    await docRef.delete(); // Remove o token inválido
    res.status(410).json({error: "Token expirado ou excedeu tentativas"});
    return;
  }

  // Incrementa a tentativa
  await docRef.update({attempts: admin.firestore.FieldValue.increment(1)});

  if (data?.user) {
    res.status(200).json({
      status: "confirmado",
      uid: data.user,
      loginTime: data.loginTime,
    });
  } else {
    res.status(202).json({status: "aguardando confirmação"});
  }
});
