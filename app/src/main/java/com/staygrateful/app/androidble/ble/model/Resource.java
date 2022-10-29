package com.staygrateful.app.androidble.ble.model;

public class Resource<T> {
    public static class Success<T> extends Resource<T> {
        T data;
        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

    public static class Error<T> extends Resource<T> {
        String errorMessage;

        public Error(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class Loading<T> extends Resource<T> {
        T data;
        String message;

        public Loading(T data, String message) {
            this.data = data;
            this.message = message;
        }

        public Loading(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }
    }
}
