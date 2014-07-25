package com.descartes.basicjsp.embed;

/**
 * A Controller factory that always returns the one instance of the Controller that is set.
 * @author fwiers
 *
 */
public class ControllerFactorySingleton implements ControllerFactory {

	private Controller singletonController;
	
	public ControllerFactorySingleton() {
		super();
	}

	public ControllerFactorySingleton(Controller singletonController) {
		super();
		this.singletonController = singletonController;
	}

	public Controller getSingletonController() {
		return singletonController;
	}

	public void setSingletonController(Controller singletonController) {
		this.singletonController = singletonController;
	}

	/**
	 * Always returns the same instance of the set controller.
	 */
	@Override
	public Controller build(String path) {
		
		return singletonController;
	}
	
}
