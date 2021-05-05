resource "aws_api_gateway_rest_api" "aws-lambda-rest-api" {
  name = "aws-lambda-rest-api-${var.environment}"
}

resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  parent_id   = aws_api_gateway_rest_api.aws-lambda-rest-api.root_resource_id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "proxy" {
  rest_api_id   = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_method.proxy.resource_id
  http_method = aws_api_gateway_method.proxy.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.aws-lambda-rest-api.invoke_arn
  depends_on              = [aws_api_gateway_method.proxy]
}

resource "aws_api_gateway_method_response" "lambda_200" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.proxy.http_method
  status_code = 200

  response_models = {
    "application/json" = "Empty"
  }
  depends_on = [aws_api_gateway_method.proxy]
}

resource "aws_api_gateway_integration_response" "lambdaResponse" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.proxy.http_method
  status_code = aws_api_gateway_method_response.lambda_200.status_code

  response_templates = {
    "application/json" = ""
  }

  depends_on = [
    aws_api_gateway_method_response.lambda_200,
    aws_api_gateway_method.proxy,
    aws_lambda_function.aws-lambda-rest-api,
  ]
}



resource "aws_api_gateway_method" "post" {
  rest_api_id   = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda_post_integration" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_method.post.resource_id
  http_method = aws_api_gateway_method.post.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.aws-lambda-rest-api.invoke_arn
  depends_on              = [aws_api_gateway_method.post]
}

resource "aws_api_gateway_method_response" "lambda_post_200" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.post.http_method
  status_code = 200

  response_models = {
    "application/json" = "Empty"
  }
  depends_on = [aws_api_gateway_method.post]
}

resource "aws_api_gateway_integration_response" "lambdaPostResponse" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.post.http_method
  status_code = aws_api_gateway_method_response.lambda_post_200.status_code

  response_templates = {
    "application/json" = ""
  }

  depends_on = [
    aws_api_gateway_method_response.lambda_post_200,
    aws_api_gateway_method.post,
    aws_lambda_function.aws-lambda-rest-api,
  ]
}



resource "aws_api_gateway_method" "optionsMethod" {
  rest_api_id   = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "options_200" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.optionsMethod.http_method
  status_code = "200"
  response_models = {
    "application/json" = "Empty"
  }
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Origin"  = true
  }
  depends_on = [aws_api_gateway_method.optionsMethod]
}

resource "aws_api_gateway_integration" "optionsIntegration" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.optionsMethod.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = "{\"statusCode\": 200}"
  }

  depends_on = [aws_api_gateway_method.optionsMethod]
}

resource "aws_api_gateway_integration_response" "optionsIntegrationResponse" {
  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.optionsMethod.http_method
  status_code = aws_api_gateway_method_response.options_200.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
    "method.response.header.Access-Control-Allow-Methods" = "'GET,OPTIONS'"
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
  }
  depends_on = [aws_api_gateway_method_response.options_200]
}

resource "aws_api_gateway_deployment" "aws-lambda-rest-api" {
  depends_on = [
    aws_api_gateway_integration.lambda,
    aws_api_gateway_integration.optionsIntegration,
  ]

  rest_api_id = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  stage_name  = var.environment
}

resource "aws_api_gateway_domain_name" "api_gateway_domain_name" {
  certificate_arn = var.certificate_arn
  domain_name     = var.domain_name
}

resource "aws_api_gateway_base_path_mapping" "api_gateway_base_path_mapping" {
  api_id      = aws_api_gateway_rest_api.aws-lambda-rest-api.id
  stage_name  = var.environment
  domain_name = aws_api_gateway_domain_name.api_gateway_domain_name.domain_name
}

resource "aws_route53_record" "api_gateway_domain" {
  zone_id = var.zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_api_gateway_domain_name.api_gateway_domain_name.cloudfront_domain_name
    zone_id                = aws_api_gateway_domain_name.api_gateway_domain_name.cloudfront_zone_id
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "api_gateway_domain_v6" {
  zone_id = var.zone_id
  name    = var.domain_name
  type    = "AAAA"

  alias {
    name                   = aws_api_gateway_domain_name.api_gateway_domain_name.cloudfront_domain_name
    zone_id                = aws_api_gateway_domain_name.api_gateway_domain_name.cloudfront_zone_id
    evaluate_target_health = true
  }
}