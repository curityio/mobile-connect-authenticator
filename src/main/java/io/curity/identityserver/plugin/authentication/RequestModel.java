/*
 *  Copyright 2017 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.curity.identityserver.plugin.authentication;

import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.web.Request;

import java.util.Optional;

public final class RequestModel
{

    @Nullable
    private final Post _postRequestModel;

    RequestModel(Request request)
    {
        _postRequestModel = request.isPostRequest() ? new Post(request) : null;
    }

    Post getPostRequestModel()
    {
        return Optional.ofNullable(_postRequestModel).orElseThrow(() ->
                new RuntimeException("Post RequestModel does not exist"));
    }

    static class Post
    {
        static final String MOBILE_NUMBER_PARAM = "mobileNumber";
        static final String OPERATOR_PARAM = "operator";

        private final String _mobileNumber;
        private final String _operator;


        Post(Request request)
        {
            if (request.getParameterNames().contains(MOBILE_NUMBER_PARAM))
            {
                _mobileNumber = request.getFormParameterValueOrError(MOBILE_NUMBER_PARAM);
                _operator = null;
            }
            else
            {
                _mobileNumber = null;
                _operator = request.getFormParameterValueOrError(OPERATOR_PARAM);
            }
        }

        String getMobileNumber()
        {
            return _mobileNumber;
        }

        String getMCCNumber()
        {
            return _operator.substring(0, 3);
        }

        String getMNCNumber()
        {
            return _operator.substring(3, _operator.length());
        }


    }
}
