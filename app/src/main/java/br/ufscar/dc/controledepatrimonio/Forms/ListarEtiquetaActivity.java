package br.ufscar.dc.controledepatrimonio.Forms;

import android.bluetooth.BluetoothDevice;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Handler;

import br.ufscar.dc.controledepatrimonio.Entity.Patrimonio;
import br.ufscar.dc.controledepatrimonio.R;
import br.ufscar.dc.controledepatrimonio.Util.RFID.DotR900.OnBtEventListener;
import br.ufscar.dc.controledepatrimonio.Util.RFID.DotR900.R900;

public class ListarEtiquetaActivity extends AppCompatActivity implements OnBtEventListener {
    private R900 leitor;
    public static final int MSG_ENABLE_LINK_CTRL = 10;
    public static final int MSG_DISABLE_LINK_CTRL = 11;
    public static final int MSG_ENABLE_DISCONNECT = 12;
    public static final int MSG_DISABLE_DISCONNECT = 13;
    public static final int MSG_SHOW_TOAST = 20;
    public static final int MSG_REFRESH_LIST_TAG = 22;
    public static final int MSG_BT_DATA_RECV = 10;
    private static final int[] TX_DUTY_OFF =
            {10, 40, 80, 100, 160, 180};

    private static final int[] TX_DUTY_ON =
            {190, 160, 70, 40, 20};

    private static final String[] TXT_DUTY =
            {"90%", "80%", "60%", "41%", "20%"};

    public static int Type;
    public static class SelectMask
    {
        public int Bank;
        public int Offset;
        public int Bits;
        public String Pattern;
        public String TagId;
    }
    public static SelectMask SelMask = new SelectMask();
    public static boolean UseMask = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_BT_DATA_RECV:
                    onNotifyBtDataRecv();
                    break;
                case MSG_SHOW_TOAST:
                    Toast.makeText(ListarEtiquetaActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_etiqueta);

        if (getIntent().hasExtra("addressDispositivo")) {
            leitor = new R900(this, mHandler, this);

            String addressDispositivo = getIntent().getExtras().getString("addressDispositivo");
            leitor.conectar(addressDispositivo);

            //region Botão RFID
            final Button btnTeste = (Button) findViewById(R.id.btnTeste);
            btnTeste.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    for (Patrimonio patrimonio : leitor.getListaPatrimonio()) {
                        Toast.makeText(getApplicationContext(), patrimonio.getTagRFID().toString(), Toast.LENGTH_LONG).show();
                    }

/*                    final String LABEL = ( (Button) v ).getText().toString();

                    if (LABEL.equalsIgnoreCase("LER")) {
                        leitor.leitura();
                        leitor.setupOperationParameter();
                        leitor.sendCmdInventory();
                        btnTeste.setText("PARAR");
                    }
                    else {
                        leitor.sendCmdStop();
                        btnTeste.setText("LER");
                    }*/

                }
            });
            //endregion
        }
    }



    @Override
    public void onNotifyBtDataRecv() {
        if (leitor == null)
            return;

        leitor.leitura();
    }

    @Override
    public void onBtConnected(BluetoothDevice device) {
        setEnabledLinkCtrl(true);

        showToastByOtherThread("Conectou: " + leitor.getDispositivo().getName(), Toast.LENGTH_SHORT);
        leitor.sendCmdOpenInterface1();

        leitor.sendSettingTxCycle(TX_DUTY_ON[0], TX_DUTY_OFF[0]);
    }

    //region MENSAGENS DO HANDLER
    private void setEnabledBtnDisconnect(boolean bEnable) {
        if (bEnable)
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_DISCONNECT, 50);
        else
            mHandler.sendEmptyMessageDelayed(MSG_DISABLE_DISCONNECT, 50);
    }

    private void setEnabledLinkCtrl(boolean bEnable) {
        if (bEnable)
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_LINK_CTRL, 50);
        else
            mHandler.sendEmptyMessageDelayed(MSG_DISABLE_LINK_CTRL, 50);
    }

    private void showToastByOtherThread(String msg, int time) {
        mHandler.removeMessages(MSG_SHOW_TOAST);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_TOAST, time, 0, msg));
    }
    //endregion

    //region MÉTODOS DA TELA
    public void onDestroy() {
        super.onDestroy();

        leitor.finalize();

    }
    //endregion

    public static SelectMask getSelectMask()
    {
        SelectMask selMask = new SelectMask();

        if( Type == 0 )
        {
            selMask.Bank = 1;//0;
            selMask.Offset = 16;//SelMask.Offset;

            final String pattern = selMask.Pattern = SelMask.TagId;
            if( pattern != null )
            {
                final int LEN = pattern.length();
                selMask.Bits = LEN * 4;
            }
            else
            {
                selMask.Bits = 0;
                selMask.Pattern = null;
            }
        }
        else
        {
            if( SelMask.Bank == 4/*0*/ )
            {
                selMask.Bits = 0;
            }
            else
            {
                selMask.Bank = SelMask.Bank;
                selMask.Offset = SelMask.Offset;

                final String pattern = selMask.Pattern = SelMask.Pattern;
                if( pattern != null )
                {
                    final int LEN = pattern.length();
                    selMask.Bits = LEN * 4;
                }
                else
                {
                    selMask.Bits = 0;
                    selMask.Pattern = null;
                }
            }
        }
        return selMask;
    }

    //region Métodos não utilizados
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_listar_etiqueta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion
}
