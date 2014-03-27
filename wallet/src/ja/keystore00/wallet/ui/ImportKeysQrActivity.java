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

import javax.annotation.Nonnull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

import ja.keystore00.wallet.R;

/**
 * @author 
 */
public final class ImportKeysQrActivity extends AbstractWalletActivity
{
    public static final String INTENT_EXTRA_INPUT = "input";

    public static void start(final Context context, @Nonnull final String input)
    {
	final Intent intent = new Intent(context, ImportKeysQrActivity.class);
	intent.putExtra(INTENT_EXTRA_INPUT, input);
	context.startActivity(intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.import_keys_qr_content);
	getWalletApplication().startBlockchainService(false);

    }
}
