package com.example.obi1.a3ade;

public class VariableChangeListener {
    private OnIntegerChangeListener listener;
    private int value;

    public interface OnIntegerChangeListener {
        public void onIntegerChanged(int newValue);
    }

    public void setOnIntegerChangeListener(OnIntegerChangeListener listener){
        this.listener = listener;
    }

    public int get(){
        return value;
    }

    public void set(int value) {
        this.value = value;
        if(listener != null){
            listener.onIntegerChanged(value);
        }
    }
}
