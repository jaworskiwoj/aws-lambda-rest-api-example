variable "region" {
  default = "eu-central-1"
}

variable "environment" {
  default = "prod"
}

variable "component" {
  default = "aws-lambda-rest-api"
}

variable "build_version" {
  default = "1.0.0"
}

variable "lambda_memory_size" {
  default = 1024
}

variable "lambda_timeout" {
  default = 30
}

variable "certificate_arn" {
}

variable "zone_id" {
}

variable "domain_name" {
}