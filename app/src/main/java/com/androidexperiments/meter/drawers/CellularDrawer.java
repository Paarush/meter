// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.androidexperiments.meter.drawers;

import android.os.Build;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

import com.androidexperiments.meter.R;

/**
 * Renders Cellular connection data, inherits from TriangleFillDrawer
 */
public class CellularDrawer extends TriangleFillDrawer {
    private final String TAG = this.getClass().getSimpleName();


    private boolean firstRead = true;
    private int overrideNetworkType = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE;

    private static boolean isAirplaneModeOn(Context context)
    {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0 ) != 0;
    }

    TelephonyManager tManager;

    public CellularDrawer(final Context context){
        super(
                context,
                context.getResources().getColor(R.color.cellular_background),
                context.getResources().getColor(R.color.cellular_triangle_background),
                context.getResources().getColor(R.color.cellular_triangle_foreground),
                context.getResources().getColor(R.color.cellular_triangle_critical)
        );

        this.label1 = "Cellular";

        tManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        setLabel2();

	int listenFlags = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE;

	if (Build.VERSION.SDK_INT >= 30) {
	    listenFlags = listenFlags | PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED;
	}

        tManager.listen(new PhoneStateListener(){
            @TargetApi(18)
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                int level = 0;
                String tech = "";

                if( isAirplaneModeOn(context) ){
                    percent = 0f;
                    connected = false;
                    label1 = "No connection";
                    label2 = "Airplane Mode Enabled";
                    return;
                }

                List<CellInfo> infos = null;

                try {
                    infos = tManager.getAllCellInfo();
                } catch (SecurityException e){
                    Log.e(TAG, e.toString());
                }

                if( infos == null ){
                    connected = false;
                    return;
                }

                for (final CellInfo info : infos) {
                    if (info instanceof CellInfoWcdma) {
                        final CellSignalStrengthWcdma  wcdma = ((CellInfoWcdma) info).getCellSignalStrength();

                        if(level < wcdma.getLevel()) {
                            level = wcdma.getLevel();
                            tech = "wcdma";
                        }
                    } else if (info instanceof CellInfoGsm) {
                        final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();

                        if(level < gsm.getLevel()) {
                            level = gsm.getLevel();
                            tech = "gsm";
                        }
                    } else if (info instanceof CellInfoCdma) {
                        final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();

                        if(level < cdma.getLevel()) {
                            level = cdma.getLevel();
                            tech = "cdma";
                        }
                    } else if (Build.VERSION.SDK_INT >= 29 && info instanceof CellInfoTdscdma) {
                        final CellSignalStrengthTdscdma tdscdma = ((CellInfoTdscdma) info).getCellSignalStrength();

                        if(level < tdscdma.getLevel()) {
                            level = tdscdma.getLevel();
                            tech = "tdscdma";
                        }
                    } else if (info instanceof CellInfoLte) {
                        final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();

                        if(level < lte.getLevel()) {
                            level = lte.getLevel();
                            tech = "lte";
                        }

                    }
                }

                connected = true;
                label1 = "Cellular";
                percent = (float) (level / 4.0);

                if (firstRead) {
                    firstRead = false;
                    _percent = (float) (percent - 0.001);
                }
            }

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);
                setLabel2();
                Log.d(TAG,"STATE "+String.valueOf(serviceState)+"   "+serviceState.getState());
            }

	    @TargetApi(30)
	    @Override
	    public void onDisplayInfoChanged(TelephonyDisplayInfo telephonyDisplayInfo) {
                try {
                    super.onDisplayInfoChanged(telephonyDisplayInfo);
		    overrideNetworkType = telephonyDisplayInfo.getOverrideNetworkType();
		} catch (SecurityException se){
		    return;
		}
	    }
        },listenFlags);

    }

    private void setLabel2() {
        String type = getNetworkType();
        label2 = tManager.getNetworkOperatorName();
        if(!type.equals("Unknown")) {
            label2 += " " + type;
        }
            try {
                label2 += " " + tManager.getNetworkType();
            } catch (SecurityException se) {
                label2 += " <SE>";
            }
    }


    @TargetApi(1)
    protected String getNetworkType(){
	try {
            int type;
	    if (Build.VERSION.SDK_INT >= 24) {
	        type = tManager.getDataNetworkType();
	    } else {
	        type = tManager.getNetworkType();
	    }
            switch (type) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
		    if (Build.VERSION.SDK_INT >= 33){
			    switch(overrideNetworkType){
				    case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE:
				    case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA:
					return "4G";
				    case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO:
					return "4G+";
				    case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA:
					return "5G";
				    case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED:
				    case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE:
					return "5G+";
			    }
		    } else {
                        return "4G";
		    }
                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G";
                default:
                    return "Unknown";
            }
	} catch (SecurityException se) {
            return "Unknown";
	}
    }

}
