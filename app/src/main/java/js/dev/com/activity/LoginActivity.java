package js.dev.com.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import js.dev.com.R;
import js.dev.com.config.ConfiguracaoFirebase;
import js.dev.com.helper.UsuarioFirebase;
import js.dev.com.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Inicializar componentes
        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
    }

    public void validarLoginUsuario(View view){
        //Recuperar textos dos campos
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        //Verificando se o texto e-mail não está vazio
        if (!textoEmail.isEmpty()){
            //Verificando se o texto senha não está vazio
            if (!textoSenha.isEmpty()){
                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario(usuario);
            }else{
                Toast.makeText(LoginActivity.this,
                        "Preencha a senha!",
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(LoginActivity.this,
                    "Preencha o e-mail!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void logarUsuario(Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //Verifica o tipo de usuário logado
                    //"Cliente" / "Técnico"
                    UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);


                }else{
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e ){
                        excecao = "Usuario não está cadastrado.";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Email e senha não correspondem";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}