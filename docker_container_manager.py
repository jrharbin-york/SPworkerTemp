import docker
import structlog
import os
import tempfile
import subprocess
import tarfile
import glob

log = structlog.get_logger()

# TODO: superclass for different container types

STATICFILE_EXTRA_PATH = "./static-var-modified"

REMOTE_CONTAINER_REGISTRIES = {
    "docker" : "192.168.1.28:5000"
}

class DockerContainerManager:
    def __init__(self, local_run_path):
        self.docker_client = docker.from_env()
        self.remote_repository = REMOTE_CONTAINER_REGISTRIES["docker"]
        self.local_run_path = local_run_path
        self.local_staticfile_path = local_run_path + STATICFILE_EXTRA_PATH

    def prepare_individual_test_containers(self, manager):
        log.warn("TODO: ensure test containers are prepared")
        return 0
        
    def ensure_downloaded_container(self, container):
        image_tag = self.remote_repository + "/" + container
        log.info("Ensuring downloading of Docker image tag:" + image_tag)
        image = self.docker_client.images.pull(image_tag)
        return image

    def ensure_downloaded_containers(self, containers):
        for c in containers:
            self.ensure_downloaded_container(c)

    def prepare_new_image(self, container_name, test_info, custom_dir_for_test):
        images = self.docker_client.images
        img = images.get(container_name)
        # Combined container name
        img.tag(container_name, tag=test_id)
        combined_name = container_name + ":" + test_id
        # Create an instance of the new image
        # Copy everything and replace all the files from the custom directory
        system_copy_files_into_image(combined_name)

    def filepaths_walk(self, start_path):
        dest_path = start_path + "/**/*"
        files_and_dir = glob.glob(dest_path, recursive=True)
        files = filter((lambda fd: os.path.isfile(fd)), files_and_dir)
        return list(files)
        
    # https://stackoverflow.com/questions/46390309/how-to-copy-a-file-from-host-to-container-using-docker-py-docker-sdk
    def system_copy_files_into_image(self, src_dir, src_image_name, dst_image_tag):
        # Need to get a temporary container for this new image
        src_image = self.docker_client.images.get(src_image_name)
        temp_container = self.docker_client.containers.create(src_image_name)
        temp_container_id = temp_container.id
        log.info("Examining source dir = " + src_dir)
        files_to_add = self.filepaths_walk(start_path=src_dir)

        if len(files_to_add) > 0:
            log.info("files_to_add = " + str(files_to_add))
            temporary_tar_file = tempfile.mktemp("-tar.tar")
            tar = tarfile.open(temporary_tar_file, mode='w')
            for file in files_to_add:
                log.debug("Adding modified file to archive: " + file)
                # Need to also remove file from image - is there a better way to do with docker.py?
                remove_cmd = "docker exec " + temp_container_id + " sh -c 'rm -f " + file + "'"
                log.debug("Removing existing file with command: " + remove_cmd)
                os.system(remove_cmd)
                tar.add(file)
            tar.close()
            tar_data_file = open(temporary_tar_file, 'rb')
            tar_data = tar_data_file.read()
            log.debug("Adding temporary tar file at " + temporary_tar_file + " to the container " + str(temp_container))
            # TODO: copying file into container - need to 
            temp_container.put_archive("/", tar_data)
            tar_data_file.close()
            dst_image_name = src_image_name + ":" + dst_image_tag
            log.debug("Commiting modified files back to the image at " + dst_image_name)
            temp_container.commit(src_image_name, dst_image_tag)
            temp_container.remove()
        else:
            log.info("No files to add for test")
            # Need to commit back the temporary files into the new image
        
    def prepare_individual_test_image(self, test_id):
        # TODO: what is the testID?
        repo_id = "test"
        log.info("Preparing additional images for test " + test_id)
        custom_dir_for_test = self.local_staticfile_path + "/" + test_id
        # For every custom container dir in the test
        for src_image_name in os.listdir(custom_dir_for_test):
            log.info("Preparing additional image for test " + test_id + " : image " + src_image_name)
            source_dir = custom_dir_for_test + "/" + src_image_name
            self.system_copy_files_into_image(source_dir, src_image_name, test_id)
        # Copy an existing image out into a new one

#def test():
#    dc = DockerContainerManager()
#    dc.prepare_individual_test_image("Test_001")
#
#test()
