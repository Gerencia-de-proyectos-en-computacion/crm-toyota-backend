package com.tricore.crmtoyota.client;

import com.sun.net.httpserver.HttpServer;
import com.tricore.crmtoyota.config.CrmProperties;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingMlForecastClientContractTest {

    @Test
    void predictBatch_inHttpMode_parsesJorgeFastApiContractAndMetrics() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();

        server.createContext("/predict/batch", exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("X-Internal-API-Key")).isEqualTo("test-key-123");
            String body = """
                    [
                      {
                        "part_id": "TOY-FIL-041",
                        "predicted_demand": 11.45,
                        "reorder_point": 9,
                        "horizon_days": 7,
                        "model_version": "0.2.0"
                      }
                    ]
                    """;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.createContext("/metrics", exchange -> {
            String body = """
                    {
                      "model_version": "0.2.0",
                      "intermittency_adi": { "mean": 1.62 },
                      "random_forest": {
                        "r2": 0.912,
                        "wape_pct": 8.45,
                        "mape_positive_pct": 9.10,
                        "rmse": 1.15
                      }
                    }
                    """;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            var props = new CrmProperties(
                    new CrmProperties.Security("secretsecretsecretsecretsecret1234", 14, "*"),
                    new CrmProperties.Ml("http", "http://127.0.0.1:" + port, "test-key-123", 5)
            );
            var client = new RoutingMlForecastClient(props, new MockMlForecastClient());
            var result = client.predictBatch(List.of(
                    new MlForecastClient.PartDemandInput(1L, "TOY-FIL-041", "04152-YZZA6", "Filtros", 4, 3, 12)
            ));

            assertThat(result.executionSource()).isEqualTo("FASTAPI_RF");
            assertThat(result.r2()).isEqualByComparingTo("0.9120");
            assertThat(result.wape()).isEqualByComparingTo("0.0845");
            assertThat(result.predictions()).hasSize(1);
            assertThat(result.predictions().getFirst().reorderPoint()).isEqualTo(9);
            assertThat(result.predictions().getFirst().suggestedOrderQty()).isGreaterThan(0);
        } finally {
            server.stop(0);
        }
    }
}
