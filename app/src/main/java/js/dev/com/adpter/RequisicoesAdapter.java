package js.dev.com.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

import js.dev.com.R;
import js.dev.com.model.Requisicao;
import js.dev.com.model.Usuario;

public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List <Requisicao> requisicoes;
    private Context context;
    private Usuario tecnico;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario tecnico) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.tecnico = tecnico;
    }

    //@NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes, parent,false);
        return new MyViewHolder(item) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
      Requisicao requisicao = requisicoes.get(position);
      Usuario cliente = requisicao.getCliente();

      holder.nome.setText(cliente.getNome());
      holder.distancia.setText("1 km - aproximadamente");
    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class  MyViewHolder extends  RecyclerView.ViewHolder{
        TextView nome, distancia;

        public MyViewHolder (View itemView){
            super(itemView);

            nome = itemView.findViewById(R.id.textRequisicaoNome);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);
        }
    }
}
