package com.gemserk.games.superflyingthing;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import com.gemserk.games.superflyingthing.scripts.ControllerUtils;

public class ControllerUtilsTest {
	
	@Test
	public void testSensibilityCalculationForAndroid() {
		assertThat(ControllerUtils.value(400f, 0, 0f, 0f, 400f), IsEqual.equalTo(-1f));
		assertThat(ControllerUtils.value(400f, 800, 0f, 0f, 400f), IsEqual.equalTo(1f));
		assertThat(ControllerUtils.value(400f, 400, 0f, 0f, 400f), IsEqual.equalTo(0f));

		assertThat(ControllerUtils.value(400f, 600, 0f, 0f, 400f), IsEqual.equalTo(0.5f));
		assertThat(ControllerUtils.value(400f, 200, 0f, 0f, 400f), IsEqual.equalTo(-0.5f));
		
		assertThat(ControllerUtils.value(400f, 600, 0.5f, 0f, 400f), IsEqual.equalTo(0.75f));
		assertThat(ControllerUtils.value(400f, 200, 0.5f, 0f, 400f), IsEqual.equalTo(-0.75f));
		
		assertThat(ControllerUtils.value(400f, 500, 0.5f, 0f, 50f), IsEqual.equalTo(1f));
		assertThat(ControllerUtils.value(400f, 425, 0.5f, 0f, 50f), IsEqual.equalTo(0.75f));
		
		assertThat(ControllerUtils.value(400f, 410, 0f, 25f, 50f), IsEqual.equalTo(0f));
		assertThat(ControllerUtils.value(400f, 410, 0.5f, 25f, 50f), IsEqual.equalTo(0.5f));
		
		assertThat(ControllerUtils.value(400f, 550, 0.5f, 100f, 200f), IsEqual.equalTo(0.75f));
		
	}
	
	@Test
	public void testSensibilityCalculationForPc() {
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility(0f, 0f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0f));
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility(0f, 1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0.2f));
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility(0f, (-1f), 0.2f, 0.001f, 1f), IsEqual.equalTo(-0.2f));
		
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility(0.2f, 1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0.201f));
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility(0.2f, 1f, 0.2f, 1f, 1f), IsEqual.equalTo(1f));

		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility((-0.2f), (-1f), 0.2f, 0.001f, 1f), IsEqual.equalTo(-0.201f));
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility((-0.2f), (-1f), 0.2f, 1f, 1f), IsEqual.equalTo(-1f));
		
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility(0.5f, 0f, 0.2f, 0.05f, 1f), IsEqual.equalTo(0f));
		
		assertThat(ControllerUtils.calculateDirectionWithVariableSensibility(0.5f, 1f, 0.2f, 0.001f, 5f), IsEqual.equalTo(0.505f));
		
	}

}
