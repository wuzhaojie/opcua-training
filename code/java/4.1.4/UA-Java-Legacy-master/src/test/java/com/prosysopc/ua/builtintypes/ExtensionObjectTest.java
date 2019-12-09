package com.prosysopc.ua.builtintypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.prosysopc.ua.core.AddNodesItem;

public class ExtensionObjectTest {

	@Test
	public void lazyEncodeEquals() throws Exception{
		ExtensionObject e1 = new ExtensionObject(new AddNodesItem());
		ExtensionObject e2 = new ExtensionObject(new AddNodesItem());
		
		assertEquals(e1, e2);
	}

}
