/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//* 
 * Copyright (C) Creactiviti LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Arik Cohen <arik@creactiviti.com>, June 2017
 */
package com.creactiviti.piper.plugin.ffmpeg;

import java.util.Map;

import com.creactiviti.piper.core.job.Job;
import org.springframework.stereotype.Component;

import com.creactiviti.piper.core.task.Task;
import com.creactiviti.piper.core.task.TaskHandler;

@Component
public class Dar implements TaskHandler<String> {

  private final Mediainfo mediainfo = new Mediainfo();
  
  @Override
  public String handle(Task aTask, Job aJob) throws Exception {
    Map<String, Object> mediainfoResult = mediainfo.handle(aTask, aJob);
    return (String) mediainfoResult.get("video_display_aspect_ratio");
  }

}

