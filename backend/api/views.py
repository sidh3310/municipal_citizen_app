from rest_framework import viewsets, status, permissions
from rest_framework.response import Response
from rest_framework.decorators import action
from rest_framework_simplejwt.views import TokenObtainPairView
from .models import User, Complaint, Department, Notification, Assignment, Announcement
from .serializers import (
    UserSerializer,
    ComplaintSerializer,
    DepartmentSerializer,
    NotificationSerializer,
    AssignmentSerializer,
    MyTokenObtainPairSerializer,
    AnnouncementSerializer
)

class MyTokenObtainPairView(TokenObtainPairView):
    serializer_class = MyTokenObtainPairSerializer

class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserSerializer

    def get_permissions(self):
        if self.action == 'create':
            return [permissions.AllowAny()]
        return [permissions.IsAdminUser()]

class ComplaintViewSet(viewsets.ModelViewSet):
    queryset = Complaint.objects.all()
    serializer_class = ComplaintSerializer
    permission_classes = [permissions.IsAuthenticated]

    @action(detail=False, methods=['get'])
    def stats(self, request):
        total = Complaint.objects.count()
        resolved = Complaint.objects.filter(status='RESOLVED').count()
        pending = Complaint.objects.exclude(status__in=['RESOLVED', 'CLOSED']).count()
        users = User.objects.count()
        return Response({
            'total_complaints': total,
            'resolved_complaints': resolved,
            'pending_complaints': pending,
            'total_users': users
        })

    def get_queryset(self):
        user = self.request.user
        if user.role == 'CITIZEN':
            return Complaint.objects.filter(user=user)
        elif user.role == 'OFFICER':
            # Officers can see all complaints or only those assigned to them.
            # Let's change this to see ALL complaints for easier testing.
            return Complaint.objects.all()
        return Complaint.objects.all()

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

class DepartmentViewSet(viewsets.ModelViewSet):
    queryset = Department.objects.all()
    serializer_class = DepartmentSerializer
    permission_classes = [permissions.IsAuthenticated]

class NotificationViewSet(viewsets.ModelViewSet):
    serializer_class = NotificationSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Notification.objects.filter(user=self.request.user)

class AssignmentViewSet(viewsets.ModelViewSet):
    queryset = Assignment.objects.all()
    serializer_class = AssignmentSerializer
    permission_classes = [permissions.IsAuthenticated]

class AnnouncementViewSet(viewsets.ModelViewSet):
    queryset = Announcement.objects.all()
    serializer_class = AnnouncementSerializer

    def get_permissions(self):
        if self.request.method in permissions.SAFE_METHODS:
            return [permissions.AllowAny()]
        return [permissions.IsAdminUser()]
