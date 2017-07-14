package org.openbase.bco.stage.jp;

/*-
 * #%L
 * BCO Stage
 * %%
 * Copyright (C) 2017 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPDisableRegistry extends org.openbase.jps.preset.AbstractJPBoolean {
    public final static String[] COMMAND_IDENTIFIERS = {"-dr", "--disable-registry"};

    public JPDisableRegistry() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public String getDescription() {
        return "If true, the program will not connect to the registry and thus not load any unit objects.";
    }
    
}