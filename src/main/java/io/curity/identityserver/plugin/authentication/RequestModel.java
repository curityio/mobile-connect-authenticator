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

import org.hibernate.validator.constraints.NotBlank;
import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.web.Request;

import javax.validation.Valid;
import java.util.Optional;

import static io.curity.identityserver.plugin.authentication.RequestModel.MobileModel.MOBILE_NUMBER_PARAM;

public final class RequestModel
{

    @Nullable
    @Valid
    private final Post _postRequestModel;

    RequestModel(Request request)
    {
        if (request.isPostRequest())
        {
            if (request.getParameterNames().contains(MOBILE_NUMBER_PARAM))
            {
                _postRequestModel = new MobileModel(request);
            }
            else
            {
                _postRequestModel = new MNOModel(request);
            }
        }
        else
        {
            _postRequestModel = null;
        }
    }

    Post getPostRequestModel()
    {
        return Optional.ofNullable(_postRequestModel).orElseThrow(() ->
                new RuntimeException("Post RequestModel does not exist"));
    }

    interface Post
    {
        String getMobileNumber();

        String getMCCNumber();

        String getMNCNumber();
    }

    class MobileModel implements Post
    {

        static final String MOBILE_NUMBER_PARAM = "mobileNumber";

        @NotBlank(message = "validation.error.mobileNumber.required")
        private final String _mobileNumber;


        MobileModel(Request request)
        {
            _mobileNumber = request.getFormParameterValueOrError(MOBILE_NUMBER_PARAM);
        }

        @Override
        public String getMobileNumber()
        {
            return _mobileNumber;
        }

        @Override
        public String getMCCNumber()
        {
            return null;
        }

        @Override
        public String getMNCNumber()
        {
            return null;
        }

    }

    class MNOModel implements Post
    {
        static final String OPERATOR_PARAM = "operator";

        @NotBlank(message = "validation.error.operator.required")
        private final String _operator;

        MNOModel(Request request)
        {
            _operator = request.getFormParameterValueOrError(OPERATOR_PARAM);
        }

        @Override
        public String getMobileNumber()
        {
            return null;
        }

        @Override
        public String getMCCNumber()
        {
            return _operator.substring(0, 3);
        }

        @Override
        public String getMNCNumber()
        {
            return _operator.substring(3, _operator.length());
        }
    }

}
