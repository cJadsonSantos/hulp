package js.dev.com.model;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import js.dev.com.config.ConfiguracaoFirebase;

public class Requisicao {
    private String id;
    private String status;
    private  Usuario cliente;
    private Usuario tecnico;
    private Destino destino;

    public  static final String STATUS_AGUARDANDO = "Aguardando Hulp";
    public  static final String STATUS_A_CAMINHO = "A caminho";
    public  static final String STATUS_VIAGEM = "Viagem";
    public  static final String STATUS_FINALIZADA = "Finalizada";

    public Requisicao() {
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();
        setId(idRequisicao);

        requisicoes.child(getId()).setValue(this);
    }

    public void atualizar (){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("tecnico", getTecnico());
        objeto.put("status", getStatus());

        requisicao.updateChildren(objeto);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public Usuario getTecnico() {
        return tecnico;
    }

    public void setTecnico(Usuario tecnico) {
        this.tecnico = tecnico;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
