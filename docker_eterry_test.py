import os
import structlog
from docker_container_manager import DockerContainerManager

def test():
    LOCAL_RUN_PATH = os.getcwd() + "/soprano_code/"
    remote_registry = "192.168.1.238:5000"
    dc = DockerContainerManager(LOCAL_RUN_PATH, remote_registry)
    dc.prepare_individual_test_image("Test_static_variable_repeat0")

test()
