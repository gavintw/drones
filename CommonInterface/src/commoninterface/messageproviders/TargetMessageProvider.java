package commoninterface.messageproviders;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.TargetMessage;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class TargetMessageProvider implements MessageProvider {

	private RobotCI robot;

	public TargetMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public TargetMessage getMessage(Message m) {

		if (m instanceof TargetMessage) {
			TargetMessage lm = (TargetMessage) m;

			if (robot instanceof AquaticDroneCI) {
				((AquaticDroneCI) robot).setUpdateEntities(lm.isToMove());
				((AquaticDroneCI) robot).setUpdateEntitiesStep(lm.getTimeStep());
			}

			if (robot.getEntityLogger() != null) {
				robot.getEntityLogger().logMessage(LogCodex.encodeLog(LogType.MESSAGE, lm.toString()));
			} else if (robot.getLogger() != null) {
				robot.getLogger().logMessage(LogCodex.encodeLog(LogType.MESSAGE, lm.toString()));
			}

			return lm;
		}

		return null;
	}

}
