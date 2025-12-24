public enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE;
    
    public static HttpMethod fromString(String method) {
        for (HttpMethod m : values()) {
            if (m.name().equals(method)) {
                return m;
            }
        }
        return null;
    }
}