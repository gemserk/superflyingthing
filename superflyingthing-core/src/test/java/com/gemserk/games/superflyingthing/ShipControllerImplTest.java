package com.gemserk.games.superflyingthing;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import com.gemserk.games.superflyingthing.scripts.UpdateControllerScript;

public class ShipControllerImplTest {
	
	@Test
	public void testSensibilityCalculationForAndroid() {
		UpdateControllerScript updateControllerScript = new UpdateControllerScript(null);
		
		assertThat(updateControllerScript.value(400f, 0, 0f, 400f), IsEqual.equalTo(-1f));
		assertThat(updateControllerScript.value(400f, 800, 0f, 400f), IsEqual.equalTo(1f));
		assertThat(updateControllerScript.value(400f, 400, 0f, 400f), IsEqual.equalTo(0f));

		assertThat(updateControllerScript.value(400f, 600, 0f, 400f), IsEqual.equalTo(0.5f));
		assertThat(updateControllerScript.value(400f, 200, 0f, 400f), IsEqual.equalTo(-0.5f));
		
		assertThat(updateControllerScript.value(400f, 600, 0.5f, 400f), IsEqual.equalTo(0.75f));
		assertThat(updateControllerScript.value(400f, 200, 0.5f, 400f), IsEqual.equalTo(-0.75f));
		
		assertThat(updateControllerScript.value(400f, 500, 0.5f, 50f), IsEqual.equalTo(1f));
		assertThat(updateControllerScript.value(400f, 425, 0.5f, 50f), IsEqual.equalTo(0.75f));
	}
	
	@Test
	public void testSensibilityCalculationForPc() {
		UpdateControllerScript updateControllerScript = new UpdateControllerScript(null);
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(0f, 0f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0f));
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(0f, 1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0.2f));
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(0f, -1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(-0.2f));
		
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(0.2f, 1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0.201f));
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(0.2f, 1f, 0.2f, 1f, 1f), IsEqual.equalTo(1f));

		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(-0.2f, -1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(-0.201f));
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(-0.2f, -1f, 0.2f, 1f, 1f), IsEqual.equalTo(-1f));
		
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(0.5f, 0f, 0.2f, 0.05f, 1f), IsEqual.equalTo(0f));
		
		assertThat(updateControllerScript.calculateDirectionWithVariableSensibility(0.5f, 1f, 0.2f, 0.001f, 5f), IsEqual.equalTo(0.505f));
		
	}

}
