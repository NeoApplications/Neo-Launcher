package android.window;
import androidx.annotation.NonNull;

public interface OnBackAnimationCallback {
    default void onBackProgressed(@NonNull BackEvent backEvent) { }
    void onBackInvoked();
    default void onBackCancelled() { }
}
