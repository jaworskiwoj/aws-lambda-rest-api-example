terraform {
  backend "s3" {
    bucket  = "terraform-state-s3-backend-prod"
    key     = "services/aws-lambda-rest-api-service"
    region  = "eu-central-1"
  }
}

provider "aws" {
  region  = var.region
}

terraform {
  required_version = ">= 0.12"
}
