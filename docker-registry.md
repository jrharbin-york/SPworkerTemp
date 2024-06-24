# Registry examples

This is the starting image ID d50cf0cb11df

It needs to be tagged with the repository IP and port, then pushed

```
docker tag d50cf0cb11df 192.168.1.28:5000/cuda_v6_remapped_scanout
docker push 192.168.1.28:5000/cuda_v6_remapped_scanout
```

Then, alternative workers can download it by:

```
docker pull 192.168.1.28:5000/cuda_v6_remapped_scanout
```

## Security
The repository is insecure by default - better solution needed

Workaround is to edit:

**/etc/docker/daemon.json**
```
{
    "insecure-registries" : ["192.168.1.28:5000"]
}
```
