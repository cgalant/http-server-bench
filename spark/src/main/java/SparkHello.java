import static spark.Spark.*;

// 4567

public final class SparkHello {
    public static void main(String[] args) {
        get("/hello", (request, response) -> {
            return "hello world";
        });
    }
}
