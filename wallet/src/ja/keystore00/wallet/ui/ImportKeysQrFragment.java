/*
 * Copyright 2014-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ja.keystore00.wallet.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Calendar;
import java.text.DateFormat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.monacoin.core.Address;
import com.google.monacoin.core.Wallet;
import com.google.monacoin.core.ECKey;

import ja.keystore00.wallet.Constants;
import ja.keystore00.wallet.WalletApplication;
import ja.keystore00.wallet.util.WalletUtils;
import ja.keystore00.wallet.R;

/**
 * @author 
 */
public final class ImportKeysQrFragment extends SherlockFragment
{
    private AbstractWalletActivity activity;
    private WalletApplication application;
    private Wallet wallet;
    private ContentResolver contentResolver;
    private LoaderManager loaderManager;

    private Button importkey,abe,cancel,send;
    private TextView date;
    private Calendar calendar;

    private int creation_year, creation_month, creation_day;

    private static final Logger log = LoggerFactory.getLogger(ImportKeysQrFragment.class);

    @Override
    public void onAttach(final Activity activity)
    {
	super.onAttach(activity);

	this.activity = (AbstractWalletActivity) activity;
	this.application = (WalletApplication) activity.getApplication();
	this.wallet = application.getWallet();
	this.contentResolver = activity.getContentResolver();
	this.loaderManager = getLoaderManager();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
	final View view = inflater.inflate(R.layout.import_keys_qr_fragment, container);
	final Intent intent = activity.getIntent();
	calendar = Calendar.getInstance();
	date = (TextView) view.findViewById(R.id.import_keys_qr_date);
	final String input = intent.getExtras().getString(ImportKeysQrActivity.INTENT_EXTRA_INPUT);
	final StringReader sr = new StringReader(input);
	final BufferedReader keyReader = new BufferedReader(sr);
	try {
	    final List<ECKey> keys = WalletUtils.readKeys(keyReader);
	    String privatekeys = new String();
	    String addresses = new String();
	    final String pubkey = new Address(Constants.NETWORK_PARAMETERS, keys.get(0).getPubKeyHash()).toString();
	    long oldest = calendar.getTimeInMillis() / 1000;
	    for (ECKey key: keys) {
		privatekeys += key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS).toString() + "\n";
		addresses += new Address(Constants.NETWORK_PARAMETERS, key.getPubKeyHash()).toString() + "\n";
		oldest = Math.min(oldest, key.getCreationTimeSeconds());
	    }
	    TextView privatekey = (TextView) view.findViewById(R.id.import_keys_qr_privatekey);
	    privatekey.setText(privatekeys);
	    TextView address = (TextView) view.findViewById(R.id.import_keys_qr_address);
	    address.setText(addresses);
	    if (oldest == 0) {
		// If key creation date is not specified
		oldest = calendar.getTimeInMillis() / 1000;
	    }
	    updateDate(oldest);
	    date.setOnClickListener(new OnClickListener()
		{
		    @Override
		    public void onClick(final View v)
		    {
			DatePickerDialog datePickerDialog = new DatePickerDialog(activity, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
			datePickerDialog.show();
		    }
		});
	    importkey = (Button) view.findViewById(R.id.import_keys_qr_importkey);
	    importkey.setOnClickListener(new OnClickListener()
		{
		    @Override
		    public void onClick(final View v)
		    {
			for (ECKey key: keys) {
			    key.setCreationTimeSeconds(calendar.getTimeInMillis()/1000);
			}
			importPrivateKeys(keys);
		    }
		});
	    abe = (Button) view.findViewById(R.id.import_keys_qr_abe);
	    abe.setOnClickListener(new OnClickListener()
		{
		    @Override
		    public void onClick(final View v)
		    {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BLOCKEXPLORER_BASE_URL + "address/" + pubkey)));

		    }
		});
	    cancel = (Button) view.findViewById(R.id.import_keys_qr_cancel);
	    cancel.setOnClickListener(new OnClickListener()
		{
		    @Override
		    public void onClick(final View v)
		    {
			activity.finish();
		    }
		});
	} catch (final IOException x) {
	    new AlertDialog.Builder(activity).setInverseBackgroundForced(true).setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.import_export_keys_dialog_failure_title)
		.setMessage(getString(R.string.import_keys_dialog_failure, x.getMessage())).setNeutralButton(R.string.button_dismiss, null)
		.show();

	    log.info("problem reading private keys", x);
	}
	return view;
    }
    private void importPrivateKeys(final List<ECKey> keys)
    {
	final int numKeysToImport = keys.size();
	final int numKeysImported = wallet.addKeys(keys);

	final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
	dialog.setInverseBackgroundForced(true);
	final StringBuilder message = new StringBuilder();
	if (numKeysImported > 0)
	    message.append(getString(R.string.import_keys_dialog_success_imported, numKeysImported));
	if (numKeysImported < numKeysToImport)
	    {
		if (message.length() > 0)
		    message.append('\n');
		message.append(getString(R.string.import_keys_dialog_success_existing, numKeysToImport - numKeysImported));
	    }
	if (numKeysImported > 0)
	    {
		if (message.length() > 0)
		    message.append("\n\n");
		message.append(getString(R.string.import_keys_dialog_success_reset));
	    }
	dialog.setMessage(message);
	if (numKeysImported > 0)
	    {
		dialog.setPositiveButton(R.string.import_keys_dialog_button_reset_blockchain, new DialogInterface.OnClickListener()
		    {
			@Override
			public void onClick(final DialogInterface dialog, final int id)
			{
			    application.resetBlockchain();
			    activity.finish();
			}
		    });
		dialog.setNegativeButton(R.string.button_dismiss, new DialogInterface.OnClickListener()
		    {
			@Override
			public void onClick(final DialogInterface dialog, final int id)
			{
			    activity.finish();
			}
		    });
	    }
	else
	    {
		dialog.setNeutralButton(R.string.button_dismiss, null);
	    }
	dialog.setOnCancelListener(null);
	dialog.show();

	log.info("imported " + numKeysImported + " of " + numKeysToImport + " private keys");
    }
    private class DateSetListener implements DatePickerDialog.OnDateSetListener
    {
	@Override
	public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day)
	{
	    updateDate(year, month, day);
	}
    };
    private final DateSetListener dateSetListener = new DateSetListener();

    private void updateDate(final int year, final int month, final int day)
    {
	calendar.set(year, month, day, 0, 0);
	date.setText(DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT).format(calendar.getTime()));
    }

    private void updateDate(final long timeSeconds)
    {
	calendar.setTimeInMillis(timeSeconds * 1000);
	date.setText(DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT).format(calendar.getTime()));
    }
}
