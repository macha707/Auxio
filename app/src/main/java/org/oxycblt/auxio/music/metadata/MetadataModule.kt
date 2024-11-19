/*
 * Copyright (c) 2023 Auxio Project
 * MetadataModule.kt is part of Auxio.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.music.metadata

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.oxycblt.auxio.music.stack.extractor.ExoPlayerTagExtractor
import org.oxycblt.auxio.music.stack.extractor.ExoPlayerTagExtractorImpl
import org.oxycblt.auxio.music.stack.extractor.TagInterpreter2
import org.oxycblt.auxio.music.stack.extractor.TagInterpreter2Impl

@Module
@InstallIn(SingletonComponent::class)
interface MetadataModule {
    @Binds fun tagInterpreter(interpreter: TagInterpreterImpl): TagInterpreter

    @Binds fun tagInterpreter2(interpreter: TagInterpreter2Impl): TagInterpreter2

    @Binds fun exoPlayerTagExtractor(extractor: ExoPlayerTagExtractorImpl): ExoPlayerTagExtractor

    @Binds fun audioPropertiesFactory(factory: AudioPropertiesFactoryImpl): AudioProperties.Factory
}
