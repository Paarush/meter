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

package com.androidexperiments.meter.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.core.content.res.ResourcesCompat;

import com.androidexperiments.meter.R;

/**
 * Render the ToggleButton as my drawable checkbox graphics
 */
public class CustomToggleButton extends AppCompatToggleButton
{

    public CustomToggleButton(Context context) {
        super(context);
    }


    public CustomToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate(){
        super.onFinishInflate();
        setText("");
        updateBackground();
    }

    private void updateBackground(){
        int drawable = isChecked() ? R.drawable.menu_checkbox_selected : R.drawable.menu_checkbox_unselected;
        setBackground(ResourcesCompat.getDrawable(getResources(), drawable, null));
    }


    @Override
    public void setChecked(boolean checked)
    {
        super.setChecked(checked);
        setText("");
        updateBackground();
    }

}
