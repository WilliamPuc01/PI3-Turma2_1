import { switchWindow } from "../home/home.js";
import { showErrorMessage, cleanError, showMessage } from "../login/login.js";

function validarString(str) {
    return str.includes('@') && str.includes('.');
}

function togglePasswordVisibility(fieldId, button) {
    const passwordField = document.getElementById(fieldId);
    const icon = button.querySelector('i');

    if (passwordField.type === "password") {
        passwordField.type = "text";
        icon.classList.remove('fa-solid', 'fa-eye');
        icon.classList.add('fa-solid', 'fa-eye-slash');
    } else {
        passwordField.type = "password";
        icon.classList.remove('fa-solid', 'fa-eye-slash');
        icon.classList.add('fa-solid', 'fa-eye');
    }
}

function senhaIgual(senha1, senha2) {
    return senha1 === senha2;
}

function isValid(name, email, password, date,password2 ){
    var valid = false;
    if(email.length > 0 && password.length >= 6 && name.length > 0 && date.length > 0 && validarString(email) && senhaIgual(password,password2) ){
        valid = true
    }
    else if(senhaIgual(password,password2) == false){
        showErrorMessage("Senhas diferentes, por favor digite senhas iguais.");
    }
    else if (validarString(email) == false){
        showErrorMessage("Email inválido.");
    }
    else if(email.length == 0 && password.length == 0 && name.length == 0 && date.length == 0){
        showErrorMessage("Por favor, preencha todos os campos.");
    }
    else if(name.length == 0){
        showErrorMessage("Por favor, digite seu nome.")
    }
    else if(email.length == 0){
        showErrorMessage("Por favor, digite seu email.")
    }
    else if(password.length == 0){
        showErrorMessage("Por favor, digite sua senha.");
    }
    else if(password.length < 6){
        showErrorMessage("Senha Invalida, no mínimo 6 caracteres.");
    }
    else if(date.length == 0){
        showErrorMessage("Por favor, selecione uma data valida.");
    }
    console.log(password.length)
    console.log(valid);
    return valid
    }

async function performSignUp(){   
    var name = document.getElementById("fieldNome").value;
    var email = document.getElementById("fieldEmail").value;
    var password = document.getElementById("fieldPassword").value;
    var date = document.getElementById("dataNascimento").value;
    var password2 = document.getElementById("fieldPassword2").value;
    email = email.trim();
    password = password.trim();

    if(isValid(name,email,password,date, password2)){
        const reqHeaders = new Headers();
        reqHeaders.append("Content-Type", "text/plain");
        reqHeaders.append("name", name);
        reqHeaders.append("email", email);
        reqHeaders.append("senha", password);
        reqHeaders.append("birthdate", date);

        const response = await fetch (
            window.IP +"/signUp",{
                method: "POST",
                headers: reqHeaders
            }
        )
        if (response.status == 200){
            cleanError();
            let message = (await response.status) + " - " + "Conta cadastrada.";
            showMessage(message);
            switchWindow('/frontend/pages/login/login.html');
        }
        else {
            let message = (await response.status) + " - " + (await response.text());
            showErrorMessage(message);
        }
    }
}
window.performSignUp = performSignUp;

window.togglePasswordVisibility = togglePasswordVisibility;


async function entrarComSuperID() {
  const apiKey = "Dd...O8wp"; // sua API real
  const siteUrl = "www.japabet.com.br";

  try {
    // Primeiro busca o QR Code
    const response = await fetch("https://us-central1-superid-760b7.cloudfunctions.net/performAuth", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ apiKey, siteUrl }),
    });

    const result = await response.json();

    if (!response.ok || !result.qrCode || !result.loginToken) {
      alert(result.error || "Erro ao gerar QR Code.");
      return;
    }

    const { qrCode, loginToken } = result;

    // Só abre a janela depois que o QR estiver pronto
    const qrWindow = window.open("", "_blank", "width=400,height=500");

    qrWindow.document.write(`
      <html>
        <head><title>SuperID Login</title></head>
        <body style="text-align: center; font-family: sans-serif;">
          <h2>Escaneie com o app SuperID</h2>
          <img src="${qrCode}" width="300" height="300" />
          <p id="status">Aguardando autenticação...</p>
        </body>
      </html>
    `);

    // Faz polling para saber se o login foi confirmado
    const interval = setInterval(async () => {
      const statusRes = await fetch("https://us-central1-superid-760b7.cloudfunctions.net/getLoginStatus", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ loginToken }),
      });

      const statusData = await statusRes.json();

      if (statusRes.status === 200 && statusData.status === "confirmado") {
        clearInterval(interval);
        qrWindow.close();
        alert("Login confirmado com SuperID!");
        window.location.href = "/frontend/pages/home/home.html";
      }

      if (statusRes.status === 410 || statusRes.status === 404) {
        clearInterval(interval);
        qrWindow.document.getElementById("status").textContent = "Token expirado ou inválido.";
        setTimeout(() => qrWindow.close(), 3000);
      }

    }, 3000);
  } catch (err) {
    console.error("Erro:", err);
    alert("Erro ao conectar com o servidor.");
  }
}

window.entrarComSuperID = entrarComSuperID;
document.getElementById("superidBtn").addEventListener("click", entrarComSuperID);