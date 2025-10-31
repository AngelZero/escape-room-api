package escape.error;

public record ApiError(int status, String error, String path) {}
