package org.openbase.bco.stage.visualization;

/*
 * -
 * #%L
 * BCO Stage
 * %%
 * Copyright (C) 2017 - 2021 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.stage.registry.JavaFX3dObjectRegistryEntry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.geometry.AxisAlignedBoundingBox3DFloatType.AxisAlignedBoundingBox3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ObjectBox implements JavaFX3dObjectRegistryEntry<String, UnitConfig> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ObjectBox.class);

    private static final Material DEFAULT_MATERIAL = PhongMaterialManager.getInstance().white;

//    private static final int COOLDOWN_TIME = 3000;
    private final Box box;

    private UnitConfig config;
//    private Thread cooldownThread;
//    private long highlightEndTime;

    public ObjectBox() {
        box = new Box();
        box.setVisible(false);
        box.setMaterial(DEFAULT_MATERIAL);
    }

    @Override
    public synchronized UnitConfig applyConfigUpdate(final UnitConfig config) throws InterruptedException, CouldNotPerformException {
        try {
            this.config = config;
            //TODO: Ask Marian how this can work, if I only synchronize the units themselves?!
            AxisAlignedBoundingBox3DFloat boundingBox = Registries.getUnitRegistry(true).getUnitShapeByUnitConfig(config).getBoundingBox();
            // TODO: Are there still visualization errors when changing the bounding box size but not the translation?
            // Previously the box was moved sometimes.

            Point3d center = Registries.getUnitRegistry(true).getUnitBoundingBoxCenterGlobalPoint3d(config).get(UnitRegistry.RCT_TIMEOUT, TimeUnit.MILLISECONDS);
            AxisAngle4d aa = new AxisAngle4d();
            aa.set(Registries.getUnitRegistry(true).getUnitRotationGlobalQuat4d(config).get(UnitRegistry.RCT_TIMEOUT, TimeUnit.MILLISECONDS));

            Platform.runLater(() -> {
                box.setVisible(true);

                box.setTranslateX(center.x);
                box.setTranslateY(center.y);
                box.setTranslateZ(center.z);

                // Depth has to be mapped to height and vv.
                box.setWidth(boundingBox.getWidth());
                box.setDepth(boundingBox.getHeight());
                box.setHeight(boundingBox.getDepth());

                //Workaround:
                if (boundingBox.getWidth() == 0.1f && boundingBox.getDepth() == 0.1f && boundingBox.getHeight() == 0.1f) {
                    box.setHeight(0.09999);
                }

                box.setRotationAxis(new Point3D(aa.x, aa.y, aa.z));
                box.setRotate(aa.angle / Math.PI * 180);
            });
            return this.config;
        } catch (NotAvailableException | TimeoutException | ExecutionException | CancellationException ex) {
            throw new CouldNotPerformException("applyConfigUpdate failed.", ex);
        }
    }

    @Override
    public synchronized UnitConfig getConfig() throws NotAvailableException {
        if (config == null) {
            throw new NotAvailableException("Config");
        }
        return config;
    }

    @Override
    public synchronized String getId() throws NotAvailableException {
        if (config == null) {
            throw new NotAvailableException("Id");
        }
        return config.getId();
    }

    @Override
    public Box getNode() {
        return box;
    }

    public synchronized void highlight(double strength) {
        if (strength < 0.5) {
            setMaterial(PhongMaterialManager.getInstance().red);
        } else if (strength < 0.65) {
            setMaterial(PhongMaterialManager.getInstance().orange);
        } else if (strength < 0.8) {
            setMaterial(PhongMaterialManager.getInstance().yellow);
        } else if (strength < 0.9) {
            setMaterial(PhongMaterialManager.getInstance().green);
        } else if (strength < 0.95) {
            setMaterial(PhongMaterialManager.getInstance().cyan);
        } else {
            setMaterial(PhongMaterialManager.getInstance().blue);
        }
        //TODO highlight longer for threshold exceeding strengths
        LOGGER.trace("highlighting ObjectBox of " + config.getLabel() + " with id: " + config.getId());
//        highlightEndTime = System.currentTimeMillis() + COOLDOWN_TIME;
//        if (cooldownThread == null || !cooldownThread.isAlive()) {
//            cooldownThread = new Thread(() -> {
//                long time_diff = getHighlightEndTime() - System.currentTimeMillis();
//                while (time_diff > 0) {
//                    try {
//                        Thread.sleep(time_diff);
//                    } catch (InterruptedException ex) {
//                        Thread.currentThread().interrupt();
//                        Controller.criticalError(ex);
//                    }
//                    time_diff = getHighlightEndTime() - System.currentTimeMillis();
//                }
//                setMaterial(DEFAULT_MATERIAL);
//            });
//            cooldownThread.start();
//        }
    }

//    private synchronized long getHighlightEndTime() {
//        return highlightEndTime;
//    }
    private void setMaterial(Material material) {
        Platform.runLater(() -> box.setMaterial(material));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(config)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ObjectBox other = (ObjectBox) obj;
        return Objects.equals(this.config, other.config);
    }
}
