package environment;

import java.util.LinkedList;
import java.util.Random;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class BoundaryEnvironment extends OpenEnvironment {
	private static final long serialVersionUID = -9033252569673228607L;
	@ArgumentsAnnotation(name = "wallsdistance", defaultValue = "5")
	protected double wallsDistance = 5;
	@ArgumentsAnnotation(name = "random", defaultValue = "0.5")
	protected double rand = 0.5;
	protected boolean placeOutside = false;
	protected String env = "square";

	protected GeoFence fence;

	public BoundaryEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		rand = args.getArgumentAsDoubleOrSetDefault("random", rand);
		wallsDistance = args.getArgumentAsDoubleOrSetDefault("wallsdistance", wallsDistance);
		placeOutside = args.getFlagIsTrue("placeoutside");
		boolean randomWidth = args.getFlagIsTrue("randomwidth");

		env = args.getArgumentAsStringOrSetDefault("environment", env);

		if (randomWidth) {
			double fitnesssample = args.getArgumentAsDouble("fitnesssample");
			double size = 1.0 + fitnesssample / 10.0;
			width *= size;
			height *= size;
			distance *= size;
			wallsDistance *= size;
		}
	}

	@Override
	public void setup(Simulator simulator) {
		fence = new GeoFence("fence");

		switch (env) {
		case "square":
			addNode(fence, -1, -1, simulator.getRandom());
			addNode(fence, -1, 0, simulator.getRandom());
			addNode(fence, -1, 1, simulator.getRandom());
			addNode(fence, 0, 1, simulator.getRandom());
			addNode(fence, 1, 1, simulator.getRandom());
			addNode(fence, 1, 0, simulator.getRandom());
			addNode(fence, 1, -1, simulator.getRandom());
			addNode(fence, 0, -1, simulator.getRandom());
			break;
		case "rectangle":// 1.667, 0.6
			addNode(fence, -1.667, -0.6, simulator.getRandom());
			addNode(fence, -1.667, 0, simulator.getRandom());
			addNode(fence, -1.667, 0.6, simulator.getRandom());
			addNode(fence, 0, 0.6, simulator.getRandom());
			addNode(fence, 1.667, 0.6, simulator.getRandom());
			addNode(fence, 1.667, 0, simulator.getRandom());
			addNode(fence, 1.667, -0.6, simulator.getRandom());
			addNode(fence, 0, -0.6, simulator.getRandom());
			break;
		case "l": // 1.1547
			addNode(fence, -1.1547, -1.1547, simulator.getRandom());
			addNode(fence, -1.1547, 0, simulator.getRandom());
			addNode(fence, -1.1547, 1.1547, simulator.getRandom());
			addNode(fence, 0, 1.1547, simulator.getRandom());
			addNode(fence, 1.1547, 1.1547, simulator.getRandom());
			addNode(fence, 1.1547, 0, simulator.getRandom());
			addNode(fence, 0, 0, simulator.getRandom());
			addNode(fence, 0, -1.1547, simulator.getRandom());
			break;

		}

		addLines(fence.getWaypoints(), simulator);

		super.setup(simulator);

		for (Robot r : robots) {
			AquaticDroneCI drone = (AquaticDroneCI) r;
			drone.getEntities().add(fence);
		}
	}

	@Override
	protected boolean safe(Robot r, Simulator simulator) {
		return super.safe(r, simulator)
				&& (placeOutside || insideLines(new Vector2d(r.getPosition().x, r.getPosition().y), simulator));
	}

	protected void addLines(LinkedList<Waypoint> waypoints, Simulator simulator) {

		for (int i = 1; i < waypoints.size(); i++) {

			Waypoint wa = waypoints.get(i - 1);
			Waypoint wb = waypoints.get(i);
			commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
			commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

			simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator, "line" + i, va.getX(),
					va.getY(), vb.getX(), vb.getY());
			addObject(l);
		}

		Waypoint wa = waypoints.get(waypoints.size() - 1);
		Waypoint wb = waypoints.get(0);
		commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

		simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator, "line0", va.getX(),
				va.getY(), vb.getX(), vb.getY());
		addObject(l);
	}

	protected void addNode(GeoFence fence, double x, double y, Random r) {

		x *= wallsDistance;
		y *= wallsDistance;

		if (rand > 0) {
			x += r.nextDouble() * rand * wallsDistance * 2 - rand * wallsDistance;
			y += r.nextDouble() * rand * wallsDistance * 2 - rand * wallsDistance;
		}
		System.out.println(x + "," + y);
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(new commoninterface.mathutils.Vector2d(x, y)));
	}

	public boolean insideLines(Vector2d v, Simulator sim) {
		// http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		for (PhysicalObject p : sim.getEnvironment().getAllObjects()) {
			if (p.getType() == PhysicalObjectType.LINE) {
				Line l = (Line) p;
				if (l.intersectsWithLineSegment(v, new Vector2d(0, -Integer.MAX_VALUE)) != null) {
					count++;
				}
			}
		}
		return count % 2 != 0;
	}

	@Override
	public void update(double time) {

	}

}
