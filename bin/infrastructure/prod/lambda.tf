data "aws_caller_identity" "current" {
}

locals {
  filename = "../../../target/${var.component}-${var.build_version}.jar"
}


resource "aws_lambda_function" "aws-lambda-rest-api" {
  function_name    = "${var.component}-${var.environment}"
  filename         = local.filename
  source_code_hash = filebase64sha256(local.filename)

  memory_size = var.lambda_memory_size
  timeout     = var.lambda_timeout

  handler = "com.example.lambda.controller.LambdaRequestHandler::handleRequest"

  runtime = "java8"

  role = aws_iam_role.aws-lambda-rest-api_role.arn

  environment {
    variables = {
      environment = var.environment
    }
  }
  tags = {
    Name = "${var.component}-${var.environment}"
  }
}

resource "aws_lambda_permission" "apigw" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.aws-lambda-rest-api.arn
  principal     = "apigateway.amazonaws.com"
  source_arn = "${replace(
    aws_api_gateway_deployment.aws-lambda-rest-api.execution_arn,
    var.environment,
    "*",
  )}/*/*"
}

resource "aws_cloudwatch_log_group" "log_group" {
  name = "/aws/lambda/${var.component}-${var.environment}"
}

resource "aws_cloudwatch_log_metric_filter" "metric_filter" {
  name           = "${var.component}-${var.environment}-errors"
  pattern        = "ERROR_LOG"
  log_group_name = aws_cloudwatch_log_group.log_group.name

  metric_transformation {
    name          = "${var.component}-${var.environment}-errors"
    namespace     = "LogMetrics"
    value         = "1"
    default_value = "0"
  }
}


resource "aws_cloudwatch_event_rule" "warm_lambda" {
  name = "${var.component}-${var.environment}-warm-lambda"
  schedule_expression = "rate(4 minutes)"
}

resource "aws_cloudwatch_event_target" "check_at_rate" {
  rule = aws_cloudwatch_event_rule.warm_lambda.name
  arn = aws_lambda_function.aws-lambda-rest-api.arn
  input = "{ \"path\": \"/objectName/1\", \"httpMethod\": \"GET\" }"
}

resource "aws_lambda_permission" "cloudwatch_call_lambda" {
  statement_id = "AllowExecutionFromCloudWatch"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.aws-lambda-rest-api.arn
  principal = "events.amazonaws.com"
  source_arn = aws_cloudwatch_event_rule.warm_lambda.arn
}