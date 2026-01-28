.class public Lwrapper/Wrapper;
.super Landroid/app/Activity;

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 3

    # super.onCreate(savedInstanceState)
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    # pm = getPackageManager()
    invoke-virtual {p0}, Landroid/app/Activity;->getPackageManager()Landroid/content/pm/PackageManager;
    move-result-object v0

    # pkg = "com.google.android.apps.camera.go"
    const-string v1, "com.google.android.apps.camera.go"

    # intent = pm.getLaunchIntentForPackage(pkg)
    invoke-virtual {v0, v1}, Landroid/content/pm/PackageManager;->getLaunchIntentForPackage(Ljava/lang/String;)Landroid/content/Intent;
    move-result-object v2

    # if intent == null goto end
    if-eqz v2, :end

    # startActivity(intent)
    invoke-virtual {p0, v2}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V

:end
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
.end method
