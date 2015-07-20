package commoninterface.entities;

import java.util.ArrayList;

import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public abstract class GeoEntity extends Entity {
	
	protected LatLon latLon;
	
	public GeoEntity(String name, LatLon latLon) {
		super(name);
		this.latLon = latLon;
	}
	
	public LatLon getLatLon() {
		return latLon;
	}

	public String getLogMessage() {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		entities.add(this);
		
		return LogCodex.encodeLog(LogType.ENTITIES,
				new LogCodex.EntityManipulation(
						LogCodex.EntityManipulation.Operation.ADD, entities,
						this.getClass().getSimpleName()));
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+";"+getLatLon().getLat()+";"+getLatLon().getLon()+";";
	}
}
