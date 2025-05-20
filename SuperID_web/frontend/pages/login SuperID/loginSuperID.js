const SUPERID_AUTH_URL = "https://performauth-nl2bfugfma-uc.a.run.app";
const SUPERID_STATUS_URL = "https://getloginstatus-nl2bfugfma-uc.a.run.app";
const SUPERID_API_KEY = "DdhxT9c1zKCAwFEPWMlFGiS8t3nPpTHVjlsdiqoNo6TmzGngO8wpmu6k4KoNyoBAgbFwmdlC3nwrB8KmCbULF4t5WHAnOdY6pwO1nODY1kgcsVN8XPvVcrazf8P1o8Pc";
const SITE_URL = "www.japabet.com.br";

// Essa função é chamada somente ao clicar em "Entrar com SuperID"
window.entrarComSuperID = async function () {
  const qrWindow = window.open("", "_blank", "width=400,height=500");

  try {
    const response = await fetch(SUPERID_AUTH_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ apiKey: SUPERID_API_KEY, siteUrl: SITE_URL })
    });

    const result = await response.json();

    if (response.ok && result.qrCode && result.loginToken) {
      const loginToken = result.loginToken;

      qrWindow.document.write(`
        <h2 style="text-align:center;">Escaneie com o app SuperID</h2>
        <img src="${result.qrCode}" style="display:block;margin:auto;width:300px;height:300px;">
        `);

      const interval = setInterval(async () => {
        try {
          const statusRes = await fetch(SUPERID_STATUS_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ loginToken })
          });

          const statusData = await statusRes.json();

          if (statusRes.ok && statusData.status === "confirmado") {
            clearInterval(interval);
            qrWindow.close();
            alert("Login autorizado via SuperID!");
          }
        } catch (err) {
          console.log("Erro ao verificar status:", err);
        }
      }, 20000);

    } else {
      qrWindow.document.write(`<p>${result.error || "Erro ao gerar QR Code."}</p>`);
    }

  } catch (err) {
    console.error(err);
    qrWindow.document.write(`<p>Erro de conexão: ${err.message}</p>`);
  }
};
