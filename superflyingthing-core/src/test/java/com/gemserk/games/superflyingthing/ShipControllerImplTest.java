package com.gemserk.games.superflyingthing;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

public class ShipControllerImplTest {
	
	@Test
	public void testSensibilityCalculationForAndroid() {
		ShipControllerImpl shipControllerImpl = new ShipControllerImpl(null);
		
		assertThat(shipControllerImpl.value(400f, 0, 0f, 400f), IsEqual.equalTo(-1f));
		assertThat(shipControllerImpl.value(400f, 800, 0f, 400f), IsEqual.equalTo(1f));
		assertThat(shipControllerImpl.value(400f, 400, 0f, 400f), IsEqual.equalTo(0f));

		assertThat(shipControllerImpl.value(400f, 600, 0f, 400f), IsEqual.equalTo(0.5f));
		assertThat(shipControllerImpl.value(400f, 200, 0f, 400f), IsEqual.equalTo(-0.5f));
		
		assertThat(shipControllerImpl.value(400f, 600, 0.5f, 400f), IsEqual.equalTo(0.75f));
		assertThat(shipControllerImpl.value(400f, 200, 0.5f, 400f), IsEqual.equalTo(-0.75f));
		
		assertThat(shipControllerImpl.value(400f, 500, 0.5f, 50f), IsEqual.equalTo(1f));
		assertThat(shipControllerImpl.value(400f, 425, 0.5f, 50f), IsEqual.equalTo(0.75f));
	}
	
	@Test
	public void testSensibilityCalculationForPc() {
		ShipControllerImpl shipControllerImpl = new ShipControllerImpl(null);
		assertThat(shipControllerImpl.valueForPc(0f, 0f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0f));
		assertThat(shipControllerImpl.valueForPc(0f, 1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0.2f));
		assertThat(shipControllerImpl.valueForPc(0f, -1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(-0.2f));
		
		assertThat(shipControllerImpl.valueForPc(0.2f, 1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(0.201f));
		assertThat(shipControllerImpl.valueForPc(0.2f, 1f, 0.2f, 1f, 1f), IsEqual.equalTo(1f));

		assertThat(shipControllerImpl.valueForPc(-0.2f, -1f, 0.2f, 0.001f, 1f), IsEqual.equalTo(-0.201f));
		assertThat(shipControllerImpl.valueForPc(-0.2f, -1f, 0.2f, 1f, 1f), IsEqual.equalTo(-1f));
		
		assertThat(shipControllerImpl.valueForPc(0.5f, 0f, 0.2f, 0.05f, 1f), IsEqual.equalTo(0f));
		
		assertThat(shipControllerImpl.valueForPc(0.5f, 1f, 0.2f, 0.001f, 5f), IsEqual.equalTo(0.505f));
		
	}

}
