package com.inter.proyecto_intergrupo.model.admin;

public class paramLDAP {

    private boolean seleccionado;
    private String Tipoparam;
    private String valorparam;

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    public String getTipoparam() {
        return Tipoparam;
    }

    public void setTipoparam(String Tipoparam) {
        this.Tipoparam = Tipoparam;
    }

    public String getValorparam() {
        return valorparam;
    }

    public void setValorparam(String valorparam) {
        this.valorparam = valorparam;
    }

    public paramLDAP(String Tipoparam, String valorparam) {
        this.Tipoparam = Tipoparam;
        this.valorparam = valorparam;
    }


}




