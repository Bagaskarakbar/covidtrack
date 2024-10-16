/*
 * Copyright 2022 Samsung Electronics Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fadhlillahb.covidtracker.wear;


import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.Value;
import com.samsung.android.service.health.tracking.data.ValueKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class TempTest {
    private final double DELTA = 0.01;

    @Mock
    TrackerDataObserver trackerDataObserver;

    @InjectMocks
    TempListener tempListener;

    @Test
    public void shouldReadValuesFromDataPoint_P() {
        //given
        final TempData tempData = new TempData(TempStatus.SUCCESSFUL_MEASUREMENT, 40.02f,38.03f);
        @SuppressWarnings("rawtypes") final Map<ValueKey, Value> values = new HashMap<>();
        values.put(ValueKey.SkinTemperatureSet.STATUS, new Value<>(tempData.status));
        values.put(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE, new Value<>(tempData.ambientTemperature));
        values.put(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE, new Value<>(tempData.wristSkinTemperature));
        final DataPoint dataPoint = new DataPoint(values);

        //when
        doAnswer(invocation -> {
            final TempData arg0 = invocation.getArgument(0);

            assertEquals(tempData.status, arg0.status);
            assertEquals(tempData.wristSkinTemperature, arg0.wristSkinTemperature, DELTA);
            assertEquals(tempData.ambientTemperature, arg0.ambientTemperature, DELTA);
            return null;
        }).when(trackerDataObserver).onSkinTemperatureChanged(any(TempData.class));

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver);
        tempListener.readValuesFromDataPoint(dataPoint);

        //then
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver);

    }
}
