package com.recovery.recovery_ai;
public class RecoverySuggestions {

    public interface SuggestionCallBack {
        void onSuggestionReceived(String suggestions);
        void onError(String error);
    }

    public static void getRecoveryAdvice(String risk, SuggestionCallBack callback) {

        GeminiRecoveryHelper.INSTANCE.getRecoveryAdvice(
                risk,
                suggestion -> {
                    callback.onSuggestionReceived(suggestion);
                    return null;
                },
                error -> {
                    callback.onError(error);
                    return null;
                }
        );
    }
}