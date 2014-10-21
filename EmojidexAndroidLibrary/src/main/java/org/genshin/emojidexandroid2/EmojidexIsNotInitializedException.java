package org.genshin.emojidexandroid2;

/**
 * Created by kou on 14/10/21.
 */
public class EmojidexIsNotInitializedException extends RuntimeException {
    public EmojidexIsNotInitializedException() {
        super("Emojidex is not initialized.Please called method of Emojidex::initialize(Context).");
    }
}
