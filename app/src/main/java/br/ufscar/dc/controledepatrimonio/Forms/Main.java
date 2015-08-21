package br.ufscar.dc.controledepatrimonio.Forms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import java.util.List;
import br.ufscar.dc.controledepatrimonio.Entity.Patrimonio;
import br.ufscar.dc.controledepatrimonio.R;
import br.ufscar.dc.controledepatrimonio.Util.Database.Database;
import br.ufscar.dc.controledepatrimonio.Util.Webservice.DepartamentoTask;
import br.ufscar.dc.controledepatrimonio.Util.Webservice.LocalTask;
import br.ufscar.dc.controledepatrimonio.Util.Webservice.PatrimonioTask;
import br.ufscar.dc.controledepatrimonio.Util.Webservice.ResponsavelTask;

public class Main extends AppCompatActivity {
    private Database db;

    private Toolbar mToolbarBootom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = new Database(Main.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Toolbar Bottom
        mToolbarBootom = (Toolbar) findViewById(R.id.ToolbarBootom);
        setSupportActionBar(mToolbarBootom);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbarBootom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_carregar:
                        carregarDados();
                        break;
                    case R.id.action_enviar:
                        enviarDados();
                        break;
                }

                return true;
            }
        });

        mToolbarBootom.inflateMenu(R.menu.menu_main);
        //endregion

        //region Botão Departamento
        Button btnDepartamento = (Button) findViewById(R.id.btnPatrimonio);
        btnDepartamento.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Main.this, PatrimonioActivity.class);
                startActivity(intent);
            }
        });
        //endregion

        //region Botão RFID
        Button btnIniciarRFID = (Button) findViewById(R.id.btnIniciarRFID);
        btnIniciarRFID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Main.this, ListarBluetoothActivity.class);
                startActivity(intent);
            }
        });
        //endregion
    }

    //region carregarDados: REALIZA O DOWNLOAD DOS DADOS DA APLICAÇÃO GRAILS
    public void carregarDados() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DepartamentoTask departamentoTask = new DepartamentoTask(Main.this);
                        departamentoTask.execute();

                        ResponsavelTask responsavelTask = new ResponsavelTask(Main.this);
                        responsavelTask.execute();

                        LocalTask localTask = new LocalTask(Main.this);
                        localTask.execute();

                        PatrimonioTask patrimonioTask = new PatrimonioTask(Main.this);
                        patrimonioTask.execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setMessage(R.string.msgCarga).setPositiveButton(R.string.opSIM, dialogClickListener).
                setNegativeButton(R.string.opNAO, dialogClickListener).show();
    }
    //endregion

    //region enviarDados: ENVIA OS DADOS DA APLICAÇÃO ANDROID PARA A APLICAÇÃO GRAILS
    public void enviarDados() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        List<Patrimonio> listaPatrimonio = db.buscarPatrimoniosParaInserir();
                        PatrimonioTask patrimonioTaskPost = new PatrimonioTask(Main.this,
                                listaPatrimonio, PatrimonioTask.TransacaoJSON.POST);

                        patrimonioTaskPost.execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setMessage(R.string.msgEnviarCarga).setPositiveButton(R.string.opSIM, dialogClickListener).
                setNegativeButton(R.string.opNAO, dialogClickListener).show();


    }
    //endregion

    //region MÉTODOS NÃO UTILIZADOS
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_carregar:

                break;
            case R.id.action_enviar:

        }

        return super.onOptionsItemSelected(item);
    }
    //endregion
}
