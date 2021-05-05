locals {
  iam_role = "aws-lambda-rest-api-${var.environment}"
}

resource "aws_iam_role" "aws-lambda-rest-api_role" {
  name = local.iam_role

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": [
          "lambda.amazonaws.com",
          "apigateway.amazonaws.com"
        ]
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF

}

resource "aws_iam_policy" "log_policy" {
  name = "${local.iam_role}-log-policy"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "logs:*",
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
EOF

}

resource "aws_iam_role_policy_attachment" "log_attach" {
  role       = aws_iam_role.aws-lambda-rest-api_role.name
  policy_arn = aws_iam_policy.log_policy.arn
}


resource "aws_iam_policy" "dynamo_db_policy" {
  name = "${local.iam_role}-dynamo_db-policy"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "dynamodb:*",
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
EOF

}

resource "aws_iam_role_policy_attachment" "dynamo_db_attach" {
  role       = aws_iam_role.aws-lambda-rest-api_role.name
  policy_arn = aws_iam_policy.dynamo_db_policy.arn
}
