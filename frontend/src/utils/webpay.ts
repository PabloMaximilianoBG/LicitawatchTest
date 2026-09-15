/**
 * Webpay Plus no es un simple redirect GET: hay que hacer un POST (formulario
 * HTML) a la "url" que entrega API MS-Ventas, con un campo oculto
 * token_ws=<token>. Este helper arma ese formulario en memoria y lo envia.
 */
export function irAWebpay(url: string, token: string): void {
  const form = document.createElement("form");
  form.method = "POST";
  form.action = url;

  const input = document.createElement("input");
  input.type = "hidden";
  input.name = "token_ws";
  input.value = token;

  form.appendChild(input);
  document.body.appendChild(form);
  form.submit();
}
