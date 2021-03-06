/*
 * Copyright 2011-2014 the original author or authors.
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

package de.langerhans.wallet.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.google.monacoin.core.NetworkParameters;

/**
 * @author Andreas Schildbach
 */
public class GenericUtils
{
	public static final BigInteger ONE_BTC = new BigInteger("100000000", 10);
	public static final BigInteger ONE_MBTC = new BigInteger("100000", 10);

	private static final int ONE_BTC_INT = ONE_BTC.intValue();
	private static final int ONE_MBTC_INT = ONE_MBTC.intValue();

	public static String formatValue(@Nonnull final BigInteger value, final int precision, final int shift)
	{
		return formatValue(value, "", "-", precision, shift);
	}

	public static String formatValue(@Nonnull final BigInteger value, @Nonnull final String plusSign, @Nonnull final String minusSign,
			final int precision, final int shift)
	{
        BigInteger newValue = value;

		final String sign = value.signum() == -1 ? minusSign : plusSign;

		if (shift == 0)
		{
			if (precision == 2)
                newValue = value.subtract(value.mod(new BigInteger("1000000"))).add(value.mod(new BigInteger("1000000")).divide(new BigInteger("500000")).multiply(new BigInteger("1000000")));
			else if (precision == 4)
                newValue = value.subtract(value.mod(new BigInteger("10000"))).add(value.mod(new BigInteger("10000")).divide(new BigInteger("5000")).multiply(new BigInteger("10000")));
			else if (precision == 6)
                newValue = value.subtract(value.mod(new BigInteger("100"))).add(value.mod(new BigInteger("100")).divide(new BigInteger("50")).multiply(new BigInteger("100")));
			else if (precision == 8)
				;
			else
				throw new IllegalArgumentException("cannot handle precision/shift: " + precision + "/" + shift);

            final BigInteger absValue = newValue.abs();
			final long coins = (absValue.divide(new BigInteger(String.valueOf(ONE_BTC_INT)))).longValue();
			final int satoshis = (absValue.mod(new BigInteger(String.valueOf(ONE_BTC_INT))).intValue());

			if (satoshis % 1000000 == 0)
				return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis / 1000000);
			else if (satoshis % 10000 == 0)
				return String.format(Locale.US, "%s%d.%04d", sign, coins, satoshis / 10000);
			else if (satoshis % 100 == 0)
				return String.format(Locale.US, "%s%d.%06d", sign, coins, satoshis / 100);
			else
				return String.format(Locale.US, "%s%d.%08d", sign, coins, satoshis);
		}
		else if (shift == 3)
		{
			if (precision == 2)
                newValue = value.subtract(value.mod(new BigInteger("1000"))).add(value.mod(new BigInteger("1000")).divide(new BigInteger("500")).multiply(new BigInteger("1000")));
			else if (precision == 4)
                newValue = value.subtract(value.mod(new BigInteger("10"))).add(value.mod(new BigInteger("10")).divide(new BigInteger("5")).multiply(new BigInteger("10")));
			else if (precision == 5)
				;
			else
				throw new IllegalArgumentException("cannot handle precision/shift: " + precision + "/" + shift);

            final BigInteger absValue = newValue.abs();
            final long coins = (absValue.divide(new BigInteger(String.valueOf(ONE_MBTC_INT)))).longValue();
            final int satoshis = (absValue.mod(new BigInteger(String.valueOf(ONE_MBTC_INT))).intValue());

			if (satoshis % 1000 == 0)
				return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis / 1000);
			else if (satoshis % 10 == 0)
				return String.format(Locale.US, "%s%d.%04d", sign, coins, satoshis / 10);
			else
				return String.format(Locale.US, "%s%d.%05d", sign, coins, satoshis);
		}
		else
		{
			throw new IllegalArgumentException("cannot handle shift: " + shift);
		}
	}

	public static BigInteger toNanoCoins(final String value, final int shift)
	{
		final BigInteger nanoCoins = new BigDecimal(value).movePointRight(8 - shift).toBigIntegerExact();

		if (nanoCoins.signum() < 0)
			throw new IllegalArgumentException("negative amount: " + value);
		if (nanoCoins.compareTo(NetworkParameters.MAX_MONEY) > 0)
			throw new IllegalArgumentException("amount too large: " + value);

		return nanoCoins;
	}
}
