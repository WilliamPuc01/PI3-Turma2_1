const SUPERID_AUTH_URL = "https://performauth-nl2bfugfma-uc.a.run.app";
  const SUPERID_STATUS_URL = "https://getloginstatus-nl2bfugfma-uc.a.run.app";
  const SUPERID_API_KEY = "DdhxT9c1zKCAwFEPWMlFGiS8t3nPpTHVjlsdiqoNo6TmzGngO8wpmu6k4KoNyoBAgbFwmdlC3nwrB8KmCbULF4t5WHAnOdY6pwO1nODY1kgcsVN8XPvVcrazf8P1o8Pc";
  const SITE_URL = "www.japabet.com.br";

  let qrWindow = null;
  let qrInterval = null;
  let qrTimeout = null;
  let superIDLoading = false;

  window.entrarComSuperID = async function () {
    if (superIDLoading) return;
    superIDLoading = true;

    if (qrWindow && !qrWindow.closed) {
      qrWindow.focus();
      superIDLoading = false;
      return;
    }

    qrWindow = window.open("", "_blank", "width=400,height=500");

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
          <p style="text-align:center;font-size:14px;">
            Essa janela se fechará automaticamente após o login ou em 1 minuto.
          </p>
        `);

        qrTimeout = setTimeout(() => {
          if (qrInterval) clearInterval(qrInterval);
          if (qrWindow && !qrWindow.closed) {
            qrWindow.document.body.innerHTML = `
              <h2 style="text-align:center;color:red;">Tempo expirado</h2>
              <p style="text-align:center;">A janela será fechada automaticamente.</p>
            `;
            setTimeout(() => qrWindow.close(), 3000);
          }
          superIDLoading = false;
        }, 60000);

        qrInterval = setInterval(async () => {
          try {
            const statusRes = await fetch(SUPERID_STATUS_URL, {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ loginToken })
            });

            const statusData = await statusRes.json();
            console.log("Status retornado:", statusData);

            if (statusRes.ok && statusData.status === "confirmado") {
              clearInterval(qrInterval);
              clearTimeout(qrTimeout);
              qrInterval = null;
              qrTimeout = null;

              if (qrWindow && !qrWindow.closed) {
                qrWindow.document.body.innerHTML = `
                  <h2 style="text-align:center;color:green;">✅ Login realizado com sucesso via SuperID!</h2>
                  <p style="text-align:center;">Esta janela será fechada automaticamente.</p>
                `;
                setTimeout(() => qrWindow.close(), 3000);
              }
              superIDLoading = false;
            } else if (statusData.status === "aguardando confirmação") {
              // continua normalmente
            } else {
              console.warn("Status inválido ou inesperado:", statusData);
              clearInterval(qrInterval);
              clearTimeout(qrTimeout);
              qrInterval = null;
              qrTimeout = null;

              if (qrWindow && !qrWindow.closed) {
                qrWindow.document.body.innerHTML = `
                  <h2 style="text-align:center;color:red;">Erro inesperado</h2>
                  <p style="text-align:center;">Tente novamente mais tarde.</p>
                `;
                setTimeout(() => qrWindow.close(), 3000);
              }
              superIDLoading = false;
            }
          } catch (err) {
            console.error("Erro ao verificar status:", err);
          }
        }, 19800); // intervalo mais seguro
      } else {
        qrWindow.document.write(`<p>${result.error || "Erro ao gerar QR Code."}</p>`);
        superIDLoading = false;
      }
    } catch (err) {
      console.error(err);
      qrWindow.document.write(`<p>Erro de conexão: ${err.message}</p>`);
      superIDLoading = false;
    }
  };