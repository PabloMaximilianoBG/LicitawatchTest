package cl.licitawatch.ventas.ambassador.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebpayPasarelaPagoAdapterTest {

    @Test
    void traduceTipoDePagoAlCatalogoMetodoPago() {
        assertThat(WebpayPasarelaPagoAdapter.metodo("VD")).isEqualTo("Débito");
        assertThat(WebpayPasarelaPagoAdapter.metodo("VP")).isEqualTo("Débito");
        assertThat(WebpayPasarelaPagoAdapter.metodo("VN")).isEqualTo("Crédito");
        assertThat(WebpayPasarelaPagoAdapter.metodo("VC")).isEqualTo("Crédito");
        assertThat(WebpayPasarelaPagoAdapter.metodo(null)).isEqualTo("Crédito");
    }
}
